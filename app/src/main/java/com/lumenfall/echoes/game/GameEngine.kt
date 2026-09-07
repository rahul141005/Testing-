package com.lumenfall.echoes.game

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.lumenfall.echoes.audio.AudioManager
import com.lumenfall.echoes.combat.WeaponRegistry
import com.lumenfall.echoes.entities.*
import com.lumenfall.echoes.progression.PlayerSave
import com.lumenfall.echoes.progression.RunManager
import com.lumenfall.echoes.progression.SaveManager
import com.lumenfall.echoes.ui.TouchControls
import com.lumenfall.echoes.utils.ObjectPool
import com.lumenfall.echoes.utils.Rect
import com.lumenfall.echoes.utils.SeededRandom
import com.lumenfall.echoes.utils.Vec2
import com.lumenfall.echoes.world.BiomeRegistry
import com.lumenfall.echoes.world.Room
import com.lumenfall.echoes.world.TileType
import kotlin.math.*

class GameEngine(
    context: Context,
    private val saveManager: SaveManager,
    private val audioManager: AudioManager
) : SurfaceView(context), SurfaceHolder.Callback {

    private var gameThread: GameThread? = null
    private var isRunning = false

    // Game state
    var gameState = GameState.MAIN_MENU
    private var playerSave = saveManager.loadPlayerSave()
    private var runManager: RunManager? = null
    private var currentRoom: Room? = null
    private var player = Player()
    private val enemies = mutableListOf<Enemy>()
    private val bosses = mutableListOf<Boss>()
    private val projectiles = mutableListOf<Projectile>()
    private val particles = mutableListOf<Particle>()
    private val lootItems = mutableListOf<LootEntity>()

    // Pools
    private val projectilePool = ObjectPool({ Projectile() }, { it.reset() }, 64)
    private val particlePool = ObjectPool({ Particle() }, { it.reset() }, 300)

    // Systems
    private var touchControls: TouchControls? = null
    private var camera = Camera()
    private var rng = SeededRandom(System.currentTimeMillis())

    // Rendering
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 36f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
    }
    private val hudPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Timing
    private var hitStopTimer = 0f
    private var cameraShake = 0f
    private var cameraShakeTimer = 0f
    private var roomTransitionTimer = 0f
    private var isTransitioning = false

    // UI state
    private var menuSelection = 0
    private var deathTimer = 0f
    private var victoryTimer = 0f

    // Loot entity
    data class LootEntity(
        var x: Float, var y: Float,
        var type: String,
        var rarity: Rarity,
        var id: String,
        var collected: Boolean = false,
        var bobOffset: Float = 0f
    )

    init {
        holder.addCallback(this)
        isFocusable = true
        isFocusableInTouchMode = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        val w = width
        val h = height
        touchControls = TouchControls(w, h, playerSave.settings.leftHanded, playerSave.settings.controlScale, playerSave.settings.controlOpacity)
        camera.setScreenSize(w.toFloat(), h.toFloat())
        startGameLoop()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        touchControls?.resize(width, height)
        camera.setScreenSize(width.toFloat(), height.toFloat())
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopGameLoop()
    }

    fun startGameLoop() {
        if (isRunning) return
        isRunning = true
        gameThread = GameThread(holder, this)
        gameThread?.start()
    }

    fun stopGameLoop() {
        isRunning = false
        try {
            gameThread?.join(500)
        } catch (e: Exception) {}
        gameThread = null
    }

    fun startNewRun() {
        playerSave = saveManager.loadPlayerSave()
        val seed = System.currentTimeMillis()
        runManager = RunManager(playerSave, seed)
        val rm = runManager!!
        currentRoom = rm.getCurrentRoom()
        player = Player().apply {
            pos.set(currentRoom!!.entryX, currentRoom!!.entryY - height)
            maxHp = 100 + if (playerSave.unlockedBiomes.contains("upgrade_health_1")) 20 else 0 + if (playerSave.unlockedBiomes.contains("upgrade_health_2")) 30 else 0
            hp = maxHp
            primaryWeapon = rm.primaryWeapon
            rangedWeapon = rm.rangedWeapon
            ability1 = rm.ability1
            ability2 = rm.ability2
            if (playerSave.unlockedBiomes.contains("upgrade_starting_coins")) {
                coins = 30
            }
        }
        spawnRoomEnemies()
        spawnRoomLoot()
        gameState = GameState.PLAYING
        camera.follow(player, true)
        audioManager.playMusic(BiomeRegistry.get(currentRoom!!.biome).musicTrack)
    }

    private fun spawnRoomEnemies() {
        enemies.clear()
        bosses.clear()
        val room = currentRoom ?: return
        val playerRef = player

        for (spawn in room.enemySpawns) {
            val enemy: Enemy = when(spawn.archetype) {
                EnemyArchetype.SHAMBLER_HUSK -> ShamblerHusk()
                EnemyArchetype.THORNLING -> Thornling()
                EnemyArchetype.GLOOM_MOTH -> GloomMoth()
                EnemyArchetype.AEGIS_REMNANT -> AegisRemnant()
                EnemyArchetype.CINDER_CHARGER -> CinderCharger()
                EnemyArchetype.RIFT_STALKER -> RiftStalker()
                EnemyArchetype.SORROW_SOWER -> SorrowSower()
                EnemyArchetype.SPIRE_WARDEN -> SpireWarden()
                EnemyArchetype.ELITE -> {
                    // pick random base and make elite
                    val base = when(rng.nextInt(3)) {
                        0 -> ShamblerHusk()
                        1 -> AegisRemnant()
                        else -> SpireWarden()
                    }
                    base.pos.set(spawn.x, spawn.y)
                    base.playerRef = playerRef
                    // create elite wrapper simplified: just boost base
                    base.maxHp = (base.maxHp*2.2f).toInt()
                    base.hp = base.maxHp
                    base.damage = (base.damage*1.5f).toInt()
                    base.isElite = true
                    base
                }
                else -> ShamblerHusk()
            }
            enemy.pos.set(spawn.x, spawn.y)
            enemy.playerRef = playerRef
            enemies.add(enemy)
        }

        // Boss room?
        if (room.type == com.lumenfall.echoes.world.RoomType.BOSS) {
            val biomeDef = BiomeRegistry.get(room.biome)
            val boss: Boss? = when(biomeDef.bossType) {
                com.lumenfall.echoes.entities.BossType.WARDEN_OF_ASH -> WardenOfAsh()
                com.lumenfall.echoes.entities.BossType.MATRIARCH_OF_ROOTS -> MatriarchOfRoots()
                com.lumenfall.echoes.entities.BossType.FORGEHEART_COLOSSUS -> ForgeheartColossus()
                else -> WardenOfAsh() // fallback
            }
            boss?.let {
                it.pos.set(room.widthTiles*Constants.TILE_SIZE*0.6f, room.entryY - it.height)
                it.playerRefGeneric = playerRef
                bosses.add(it)
            }
        }
    }

    private fun spawnRoomLoot() {
        lootItems.clear()
        val room = currentRoom ?: return
        for (loot in room.lootSpawns) {
            lootItems.add(LootEntity(loot.x, loot.y, loot.type, loot.rarity, loot.type + "_" + rng.nextInt(10000)))
        }
    }

    fun update(dt: Float) {
        if (hitStopTimer > 0f) {
            hitStopTimer -= dt
            if (hitStopTimer > 0f) return // freeze
        }

        when(gameState) {
            GameState.MAIN_MENU -> updateMenu(dt)
            GameState.PLAYING -> updatePlaying(dt)
            GameState.PAUSED -> updatePaused(dt)
            GameState.DEATH -> updateDeath(dt)
            GameState.VICTORY -> updateVictory(dt)
            GameState.BIOME_TRANSITION -> updateBiomeTransition(dt)
            else -> updatePlaying(dt)
        }

        // camera shake decay
        if (cameraShakeTimer > 0f) {
            cameraShakeTimer -= dt
            if (cameraShakeTimer <= 0f) cameraShake = 0f
        }
    }

    private fun updateMenu(dt: Float) {
        // simple auto-demo? Wait for input to start new run handled via touch
    }

    private fun updatePlaying(dt: Float) {
        val rm = runManager ?: return
        val room = currentRoom ?: return

        // Input
        val input = touchControls?.consumeInputs()
        if (input != null) {
            if (input.pause) {
                gameState = GameState.PAUSED
                return
            }
            val moveX = input.moveX
            player.setInput(
                moveX = moveX,
                jump = input.jump,
                dash = input.dash,
                attack = input.attack,
                ability1In = input.ability1,
                ability2In = input.ability2,
                ranged = false,
                jumpHeldIn = false // simplified
            )

            // Interact - room exit
            if (input.interact) {
                checkRoomExit()
            }
        }

        // Player update
        player.update(dt)

        // Collision with tilemap
        handleTileCollision(player, room)

        // Enemies update
        for (enemy in enemies.toList()) {
            if (!enemy.active) continue
            enemy.update(dt)
            handleTileCollision(enemy, room)
            // Enemy vs player damage via AI already, but also check collision
        }
        for (boss in bosses.toList()) {
            if (!boss.active) continue
            boss.update(dt)
            handleTileCollision(boss, room)
        }

        // Projectiles
        for (proj in projectiles.toList()) {
            proj.update(dt)
            if (!proj.active) {
                projectilePool.free(proj)
                projectiles.remove(proj)
                continue
            }
            // tile collision
            if (checkProjectileTileCollision(proj, room)) {
                createImpactParticles(proj.pos.x, proj.pos.y, 6)
                projectilePool.free(proj)
                projectiles.remove(proj)
                continue
            }
            // entity collision
            if (proj.fromPlayer) {
                // hit enemies
                for (enemy in enemies) {
                    if (!enemy.active) continue
                    if (proj.bounds.intersects(enemy.bounds)) {
                        enemy.takeDamage(proj.damage, Vec2(proj.vel.x*0.2f, proj.vel.y*0.2f), player)
                        createBloodParticles(enemy.center.x, enemy.center.y, 5, enemy.facing)
                        triggerHitStop(false, false)
                        triggerCameraShake(Constants.CAM_SHAKE_LIGHT)
                        proj.onHit()
                        if (!proj.active) break
                    }
                }
                for (boss in bosses) {
                    if (!boss.active) continue
                    if (proj.bounds.intersects(boss.bounds)) {
                        boss.takeDamage(proj.damage, Vec2(proj.vel.x*0.2f, proj.vel.y*0.2f), player)
                        createBloodParticles(boss.center.x, boss.center.y, 8, boss.facing)
                        triggerHitStop(true, false)
                        triggerCameraShake(Constants.CAM_SHAKE_HEAVY)
                        proj.onHit()
                        if (!proj.active) break
                    }
                }
            } else {
                // enemy projectile vs player
                if (proj.bounds.intersects(player.bounds) && !player.isInvulnerable()) {
                    player.takeDamage(proj.damage, Vec2(proj.vel.x*0.2f, proj.vel.y*0.2f), null)
                    createBloodParticles(player.center.x, player.center.y, 6, player.facing)
                    triggerCameraShake(Constants.CAM_SHAKE_LIGHT)
                    projectilePool.free(proj)
                    projectiles.remove(proj)
                }
            }
        }

        // Player hitboxes vs enemies
        for (hb in player.activeHitboxes) {
            for (enemy in enemies) {
                if (!enemy.active) continue
                if (hb.rect.intersects(enemy.bounds)) {
                    enemy.takeDamage(hb.damage, hb.knockback, player)
                    createBloodParticles(enemy.center.x, enemy.center.y, if (hb.isCrit) 12 else 6, enemy.facing)
                    createSlashParticles(hb.rect.centerX, hb.rect.centerY, hb.isCrit)
                    triggerHitStop(hb.isCrit, hb.damage > 30)
                    triggerCameraShake(if (hb.isCrit) Constants.CAM_SHAKE_HEAVY else Constants.CAM_SHAKE_LIGHT)
                    rm.runStats.damageDealt += hb.damage
                }
            }
            for (boss in bosses) {
                if (!boss.active) continue
                if (hb.rect.intersects(boss.bounds)) {
                    boss.takeDamage(hb.damage, hb.knockback, player)
                    createBloodParticles(boss.center.x, boss.center.y, if (hb.isCrit) 14 else 8, boss.facing)
                    createSlashParticles(hb.rect.centerX, hb.rect.centerY, hb.isCrit)
                    triggerHitStop(hb.isCrit, true)
                    triggerCameraShake(Constants.CAM_SHAKE_HEAVY)
                    rm.runStats.damageDealt += hb.damage
                }
            }
        }

        // Enemy projectiles spawning (simplified: thornling, sorrow)
        for (enemy in enemies) {
            if (enemy is Thornling && enemy.state == EnemyState.ATTACK && enemy.stateTime in 0.39f..0.41f) {
                spawnProjectile(enemy.center.x, enemy.center.y, enemy.facing*280f, -30f, enemy.damage, DamageType.PHYSICAL, false, ProjectileType.THORN_SPIT)
            }
            if (enemy is SorrowSower) {
                if (enemy.state == EnemyState.ATTACK && enemy.stateTime in 0.49f..0.51f) {
                    spawnProjectile(enemy.center.x, enemy.center.y, enemy.facing*200f, -20f, enemy.damage, DamageType.VOID, false, ProjectileType.SORROW_ORB)
                }
                if (enemy.state == EnemyState.SPECIAL && enemy.stateTime in 0.99f..1.01f) {
                    // summon 2 shamblers
                    repeat(2) {
                        val sham = ShamblerHusk()
                        sham.pos.set(enemy.pos.x + (if (it==0) -40f else 40f), enemy.pos.y)
                        sham.playerRef = player
                        enemies.add(sham)
                    }
                }
            }
        }

        // Loot collection
        for (loot in lootItems.toList()) {
            if (loot.collected) continue
            loot.bobOffset += dt*3f
            val dx = player.center.x - (loot.x + 16f)
            val dy = player.center.y - (loot.y + 16f)
            val dist = sqrt(dx*dx + dy*dy)
            if (dist < 48f) {
                // collect
                loot.collected = true
                when(loot.type) {
                    "coin" -> {
                        rm.addCoins(5 + rng.nextInt(8))
                        player.coins += 5
                        createCoinParticles(loot.x, loot.y, 6)
                        audioManager.playPickup()
                    }
                    "weapon" -> {
                        val newW = WeaponRegistry.getRandom(Rarity.RARE, rng)
                        // offer swap? For now auto equip if better rarity
                        if (newW.rarity.ordinal >= player.primaryWeapon.rarity.ordinal) {
                            player.primaryWeapon = newW
                            rm.primaryWeapon = newW
                        }
                        createHealParticles(loot.x, loot.y)
                    }
                    "ability" -> {
                        val newA = com.lumenfall.echoes.combat.AbilityRegistry.getRandom(rng)
                        player.ability1 = newA
                        rm.ability1 = newA
                        createHealParticles(loot.x, loot.y)
                    }
                    "heal" -> {
                        player.heal(30)
                        createHealParticles(loot.x, loot.y)
                    }
                }
            }
        }
        lootItems.removeAll { it.collected }

        // Particles
        for (p in particles.toList()) {
            p.update(dt)
            if (!p.active) {
                particlePool.free(p)
                particles.remove(p)
            }
        }

        // Check room cleared
        val allDead = enemies.all { !it.active } && bosses.all { !it.active }
        if (allDead && !room.isCleared) {
            room.isCleared = true
            rm.runStats.roomsCleared++
            // spawn extra loot
            if (rng.chance(0.6f)) {
                lootItems.add(LootEntity(room.exitX - 40f, room.entryY - 40f, "coin", Rarity.COMMON, "coin_bonus"))
            }
        }

        // Check player death
        if (player.state == PlayerState.DEAD || player.hp <= 0) {
            gameState = GameState.DEATH
            deathTimer = 0f
            playerSave.totalDeaths++
            playerSave.totalRuns++
            saveManager.savePlayerSave(playerSave)
            saveManager.clearRunSave()
        }

        // Camera follow
        camera.follow(player, false)
        camera.update(dt, cameraShake)

        // Save run periodically
        rm.runStats.timePlayed += dt
        if (rm.runStats.timePlayed % 5f < dt) {
            saveManager.saveRunSave(rm.toRunSave().apply { playerHp = player.hp; playerMaxHp = player.maxHp })
        }
    }

    private fun updatePaused(dt: Float) {
        // handle unpause via touchControls
        val input = touchControls?.consumeInputs()
        if (input?.pause == true || input?.interact == true) {
            gameState = GameState.PLAYING
        }
    }

    private fun updateDeath(dt: Float) {
        deathTimer += dt
        if (deathTimer > 2.5f) {
            // go to menu
            gameState = GameState.MAIN_MENU
            // reward shards based on progress
            val rm = runManager
            if (rm != null) {
                val shards = (rm.coins * 0.2f).toInt() + rm.runStats.roomsCleared*2
                playerSave.lumenShards += shards
                playerSave.totalCoinsEarned += rm.coins
                saveManager.savePlayerSave(playerSave)
            }
        }
    }

    private fun updateVictory(dt: Float) {
        victoryTimer += dt
        if (victoryTimer > 3f) gameState = GameState.MAIN_MENU
    }

    private fun updateBiomeTransition(dt: Float) {
        roomTransitionTimer += dt
        if (roomTransitionTimer > 1.2f) {
            isTransitioning = false
            gameState = GameState.PLAYING
            roomTransitionTimer = 0f
        }
    }

    private fun checkRoomExit() {
        val room = currentRoom ?: return
        val rm = runManager ?: return
        val distToExit = Vec2(room.exitX, room.exitY).distance(player.pos)
        if (distToExit < 80f && room.isCleared) {
            // advance
            if (rm.advanceRoom()) {
                currentRoom = rm.getCurrentRoom()
                player.pos.set(currentRoom!!.entryX, currentRoom!!.entryY - player.height)
                spawnRoomEnemies()
                spawnRoomLoot()
                // biome transition?
                if (currentRoom!!.type == com.lumenfall.echoes.world.RoomType.TRANSITION) {
                    gameState = GameState.BIOME_TRANSITION
                    roomTransitionTimer = 0f
                    isTransitioning = true
                }
                // music
                currentRoom?.let {
                    audioManager.playMusic(BiomeRegistry.get(it.biome).musicTrack)
                }
            } else {
                // victory - completed all biomes
                gameState = GameState.VICTORY
                victoryTimer = 0f
                playerSave.lumenShards += 200 + rm.coins
                playerSave.bestBiomeReached = max(playerSave.bestBiomeReached, rm.currentBiomeIndex)
                saveManager.savePlayerSave(playerSave)
                saveManager.clearRunSave()
            }
        }
    }

    private fun handleTileCollision(entity: Entity, room: Room) {
        // simple AABB vs tilemap solids
        // X axis
        entity.pos.x += entity.vel.x * Constants.FIXED_DT
        var collidedX = false
        for (solid in room.tileMap.solids) {
            if (entity.bounds.intersects(solid)) {
                if (entity.vel.x > 0) {
                    entity.pos.x = solid.left - entity.width
                } else if (entity.vel.x < 0) {
                    entity.pos.x = solid.right
                }
                entity.vel.x = 0f
                collidedX = true
                if (entity is Player) {
                    // wall slide check
                    if (!entity.grounded && abs(entity.inputX) > 0.1f) {
                        entity.isWallSliding = true
                        entity.wallDir = if (entity.vel.x > 0) 1 else -1 // actually need direction of wall
                        // determine wall dir from solid position
                        entity.wallDir = if (solid.centerX > entity.center.x) 1 else -1
                    }
                }
            }
        }
        if (!collidedX && entity is Player) {
            entity.isWallSliding = false
        }

        // Y axis
        entity.pos.y += entity.vel.y * Constants.FIXED_DT
        entity.grounded = false
        for (solid in room.tileMap.solids) {
            if (entity.bounds.intersects(solid)) {
                if (entity.vel.y > 0) {
                    // landing
                    entity.pos.y = solid.top - entity.height
                    entity.vel.y = 0f
                    entity.grounded = true
                } else if (entity.vel.y < 0) {
                    entity.pos.y = solid.bottom
                    entity.vel.y = 0f
                }
            }
        }

        // world bounds
        if (entity.pos.y > room.heightTiles*Constants.TILE_SIZE + 200f) {
            // fell out
            if (entity is Player) {
                entity.takeDamage(20, Vec2(0f,-100f), null)
                entity.pos.set(room.entryX, room.entryY - entity.height)
                entity.vel.set(0f,0f)
            } else {
                entity.active = false
            }
        }
        if (entity.pos.x < 0f) entity.pos.x = 0f
        if (entity.pos.x > room.widthTiles*Constants.TILE_SIZE - entity.width) entity.pos.x = room.widthTiles*Constants.TILE_SIZE - entity.width
    }

    private fun checkProjectileTileCollision(proj: Projectile, room: Room): Boolean {
        for (solid in room.tileMap.solids) {
            if (proj.bounds.intersects(solid)) return true
        }
        return false
    }

    private fun spawnProjectile(x: Float, y: Float, vx: Float, vy: Float, dmg: Int, type: DamageType, fromPlayer: Boolean, projType: ProjectileType) {
        val p = projectilePool.obtain()
        p.launch(x,y,vx,vy,dmg,type,fromPlayer,projType)
        p.maxLife = 4f
        projectiles.add(p)
    }

    private fun createImpactParticles(x: Float, y: Float, count: Int) {
        repeat(count) {
            val p = particlePool.obtain()
            val ang = (rng.nextFloat()*Math.PI*2).toFloat()
            val spd = rng.nextFloat(60f, 220f)
            p.init(x,y, cos(ang)*spd, sin(ang)*spd, rng.nextFloat(0.2f,0.5f), ParticleType.IMPACT, 0xAAAAAA, 3f, 0.5f, 200f)
            particles.add(p)
        }
    }

    private fun createBloodParticles(x: Float, y: Float, count: Int, dir: Int) {
        repeat(count) {
            val p = particlePool.obtain()
            val ang = (rng.nextFloat()*Math.PI - Math.PI*0.5).toFloat()
            val spd = rng.nextFloat(40f, 180f)
            p.init(x,y, cos(ang)*spd*dir, sin(ang)*spd - 30f, rng.nextFloat(0.3f,0.6f), ParticleType.BLOOD, 0xC73A3A, 4f, 1f, 400f)
            particles.add(p)
        }
    }

    private fun createSlashParticles(x: Float, y: Float, isCrit: Boolean) {
        val col = if (isCrit) 0xFFFFD700.toInt() else 0xFFFFFFFF.toInt()
        repeat(if(isCrit) 8 else 4) {
            val p = particlePool.obtain()
            val ang = (rng.nextFloat()*Math.PI*2).toFloat()
            val spd = rng.nextFloat(80f, if(isCrit) 320f else 200f)
            p.init(x,y, cos(ang)*spd, sin(ang)*spd, rng.nextFloat(0.15f,0.35f), ParticleType.SLASH, col, if(isCrit) 6f else 3f, 0.5f, 0f)
            particles.add(p)
        }
    }

    private fun createCoinParticles(x: Float, y: Float, count: Int) {
        repeat(count) {
            val p = particlePool.obtain()
            p.init(x,y, rng.nextFloat(-120f,120f), rng.nextFloat(-200f,-40f), rng.nextFloat(0.5f,0.9f), ParticleType.COIN, 0xFFD700, 5f, 2f, 300f)
            particles.add(p)
        }
    }

    private fun createHealParticles(x: Float, y: Float) {
        repeat(10) {
            val p = particlePool.obtain()
            p.init(x,y, rng.nextFloat(-80f,80f), rng.nextFloat(-180f,-20f), rng.nextFloat(0.6f,1f), ParticleType.HEAL, 0x4ADE80, 4f, 1f, -80f)
            particles.add(p)
        }
    }

    private fun triggerHitStop(isCrit: Boolean, isHeavy: Boolean) {
        if (playerSave.settings.reducedEffects) return
        hitStopTimer = when {
            isCrit && isHeavy -> Constants.HIT_STOP_HEAVY
            isCrit || isHeavy -> 0.07f
            else -> Constants.HIT_STOP_LIGHT
        }
    }

    private fun triggerCameraShake(intensity: Float) {
        if (!playerSave.settings.screenShake) return
        cameraShake = max(cameraShake, intensity)
        cameraShakeTimer = 0.25f
    }

    // Rendering
    fun render(canvas: Canvas) {
        if (canvas == null) return
        canvas.drawColor(Color.BLACK)

        when(gameState) {
            GameState.MAIN_MENU -> renderMenu(canvas)
            GameState.PLAYING, GameState.PAUSED, GameState.BIOME_TRANSITION -> renderGame(canvas)
            GameState.DEATH -> renderDeath(canvas)
            GameState.VICTORY -> renderVictory(canvas)
            else -> renderGame(canvas)
        }

        // Touch controls overlay
        if (gameState == GameState.PLAYING) {
            touchControls?.let { renderTouchControls(canvas, it) }
        }
    }

    private fun renderMenu(canvas: Canvas) {
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()

        // Background gradient
        val bgPaint = Paint().apply {
            shader = LinearGradient(0f,0f,0f,h, Color.parseColor("#0A0A12"), Color.parseColor("#1E1E2E"), Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f,0f,w,h, bgPaint)

        // Title
        paint.color = Color.parseColor("#7DE2FF")
        paint.textSize = 92f
        paint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("LUMENFALL", w/2, h*0.28f, paint)

        paint.color = Color.parseColor("#FFC857")
        paint.textSize = 28f
        canvas.drawText("Echoes of the Shifting Citadel", w/2, h*0.34f, paint)

        // Menu options
        val options = listOf("Begin Echo", "Codex", "Settings", "Quit")
        paint.textSize = 42f
        for ((i, opt) in options.withIndex()) {
            paint.color = if (i==menuSelection) Color.WHITE else Color.parseColor("#9AA0B0")
            if (i==menuSelection) {
                paint.color = Color.parseColor("#7DE2FF")
                canvas.drawText("▶ $opt", w/2, h*0.5f + i*70f, paint)
            } else {
                canvas.drawText(opt, w/2, h*0.5f + i*70f, paint)
            }
        }

        // Stats
        paint.textSize = 22f
        paint.color = Color.parseColor("#9AA0B0")
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Runs: ${playerSave.totalRuns}  Shards: ${playerSave.lumenShards}  Best: ${playerSave.bestBiomeReached}", 40f, h-40f, paint)

        // Lore snippet
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 20f
        paint.color = Color.parseColor("#6A6A7A")
        canvas.drawText("You awaken again. The Citadel has shifted. Your echo remains.", w/2, h*0.42f, paint)
    }

    private fun renderGame(canvas: Canvas) {
        val room = currentRoom ?: return
        val biomeDef = BiomeRegistry.get(room.biome)

        // Background
        canvas.drawColor(biomeDef.colorBackground)

        // Parallax layers (simple)
        paint.color = adjustColor(biomeDef.colorBackground, 1.2f)
        for (i in 0..3) {
            val offset = camera.x * (0.1f + i*0.1f)
            canvas.drawRect(-offset + i*400f, 0f, -offset + i*400f + 300f, canvas.height.toFloat(), paint)
        }

        canvas.save()
        canvas.translate(-camera.x, -camera.y)

        // Tilemap
        renderTileMap(canvas, room)

        // Exit marker
        paint.color = if (room.isCleared) Color.parseColor("#4ADE80") else Color.parseColor("#6B7280")
        paint.style = Paint.Style.FILL
        canvas.drawRect(room.exitX, room.exitY - 80f, room.exitX + 24f, room.exitY + 16f, paint)
        if (room.isCleared) {
            paint.color = Color.WHITE
            paint.textSize = 18f
            canvas.drawText("EXIT", room.exitX - 8f, room.exitY - 90f, paint)
        }

        // Loot
        for (loot in lootItems) {
            val bob = sin(loot.bobOffset)*6f
            when(loot.type) {
                "coin" -> {
                    paint.color = Color.parseColor("#FFD700")
                    canvas.drawCircle(loot.x+12f, loot.y+12f+bob, 10f, paint)
                    paint.color = Color.parseColor("#FFA500")
                    canvas.drawCircle(loot.x+12f, loot.y+12f+bob, 6f, paint)
                }
                "weapon" -> {
                    paint.color = Color.parseColor("#7DE2FF")
                    canvas.drawRect(loot.x, loot.y+bob, loot.x+28f, loot.y+12f+bob, paint)
                    paint.color = Color.WHITE
                    canvas.drawRect(loot.x+6f, loot.y-8f+bob, loot.x+10f, loot.y+12f+bob, paint)
                }
                "ability" -> {
                    paint.color = Color.parseColor("#B07FFF")
                    canvas.drawCircle(loot.x+14f, loot.y+14f+bob, 14f, paint)
                    paint.color = Color.WHITE
                    canvas.drawCircle(loot.x+14f, loot.y+14f+bob, 6f, paint)
                }
                "heal" -> {
                    paint.color = Color.parseColor("#4ADE80")
                    canvas.drawCircle(loot.x+12f, loot.y+12f+bob, 12f, paint)
                    paint.color = Color.WHITE
                    paint.textSize = 16f
                    canvas.drawText("+", loot.x+8f, loot.y+18f+bob, paint)
                }
            }
        }

        // Enemies
        for (enemy in enemies) {
            if (!enemy.active) continue
            renderEnemy(canvas, enemy)
        }
        for (boss in bosses) {
            if (!boss.active) continue
            renderBoss(canvas, boss)
        }

        // Projectiles
        paint.style = Paint.Style.FILL
        for (proj in projectiles) {
            if (!proj.active) continue
            paint.color = when(proj.type) {
                ProjectileType.ARROW -> Color.parseColor("#EAEAEA")
                ProjectileType.BOLT -> Color.parseColor("#FF6B35")
                ProjectileType.EMBER -> Color.parseColor("#FF4500")
                ProjectileType.SHARD -> Color.parseColor("#7DE2FF")
                ProjectileType.THORN_SPIT -> Color.parseColor("#7FB069")
                ProjectileType.SORROW_ORB -> Color.parseColor("#B07FFF")
                else -> Color.WHITE
            }
            canvas.drawRect(proj.pos.x, proj.pos.y, proj.pos.x+proj.width, proj.pos.y+proj.height, paint)
        }

        // Player
        renderPlayer(canvas, player)

        // Player hitboxes (debug - could show)
        // for (hb in player.activeHitboxes) {
        //     paint.color = Color.argb(80, 255, 255, 0)
        //     canvas.drawRect(hb.rect.x, hb.rect.y, hb.rect.right, hb.rect.bottom, paint)
        // }

        // Particles
        for (p in particles) {
            if (!p.active) continue
            paint.color = Color.argb(p.a, p.r, p.g, p.b)
            when(p.type) {
                ParticleType.SLASH -> {
                    paint.strokeWidth = p.size
                    paint.style = Paint.Style.STROKE
                    canvas.drawLine(p.pos.x, p.pos.y, p.pos.x + p.vel.x*0.05f, p.pos.y + p.vel.y*0.05f, paint)
                    paint.style = Paint.Style.FILL
                }
                else -> {
                    canvas.drawCircle(p.pos.x, p.pos.y, p.size, paint)
                }
            }
        }

        canvas.restore()

        // HUD
        renderHUD(canvas)

        if (gameState == GameState.PAUSED) {
            // pause overlay
            paint.color = Color.argb(160, 0,0,0)
            canvas.drawRect(0f,0f,canvas.width.toFloat(), canvas.height.toFloat(), paint)
            paint.color = Color.WHITE
            paint.textSize = 64f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("PAUSED", canvas.width/2f, canvas.height/2f, paint)
            paint.textSize = 28f
            canvas.drawText("Tap to resume", canvas.width/2f, canvas.height/2f + 60f, paint)
        }

        if (isTransitioning) {
            val alpha = (roomTransitionTimer / 1.2f * 255).toInt().coerceIn(0,255)
            paint.color = Color.argb(alpha, 0,0,0)
            canvas.drawRect(0f,0f,canvas.width.toFloat(), canvas.height.toFloat(), paint)
            paint.color = Color.WHITE
            paint.textSize = 48f
            paint.textAlign = Paint.Align.CENTER
            val biomeName = currentRoom?.let { BiomeRegistry.get(it.biome).name } ?: "???"
            canvas.drawText(biomeName, canvas.width/2f, canvas.height/2f, paint)
        }
    }

    private fun renderTileMap(canvas: Canvas, room: Room) {
        val biome = BiomeRegistry.get(room.biome)
        // solids
        paint.style = Paint.Style.FILL
        for (solid in room.tileMap.solids) {
            // differentiate platform vs solid by height
            if (solid.h < 12f) {
                paint.color = biome.colorPrimary
            } else {
                paint.color = biome.colorSecondary
                // darken a bit
                paint.color = adjustColor(paint.color, 0.8f)
            }
            canvas.drawRect(solid.x, solid.y, solid.right, solid.bottom, paint)
            // top highlight
            paint.color = adjustColor(biome.colorPrimary, 1.3f)
            canvas.drawRect(solid.x, solid.y, solid.right, solid.y+4f, paint)
        }
    }

    private fun renderPlayer(canvas: Canvas, p: Player) {
        val x = p.pos.x
        val y = p.pos.y
        val w = p.width
        val h = p.height

        // shadow
        paint.color = Color.argb(60,0,0,0)
        canvas.drawOval(x-4f, y+h-6f, x+w+4f, y+h+6f, paint)

        // body - stylized armor
        val isInvuln = p.isInvulnerable() && (System.currentTimeMillis()/80 % 2L == 0L)
        if (!isInvuln) {
            // legs
            paint.color = Color.parseColor("#2A2A40")
            canvas.drawRect(x+4f, y+h*0.55f, x+w-4f, y+h, paint)
            // torso
            paint.color = Color.parseColor("#7DE2FF")
            canvas.drawRect(x+2f, y+8f, x+w-2f, y+h*0.65f, paint)
            // head
            paint.color = Color.parseColor("#EAEAEA")
            canvas.drawRect(x+6f, y, x+w-6f, y+14f, paint)
            // visor
            paint.color = Color.parseColor("#FFC857")
            if (p.facing>0) canvas.drawRect(x+w-14f, y+4f, x+w-4f, y+10f, paint)
            else canvas.drawRect(x+4f, y+4f, x+14f, y+10f, paint)

            // weapon
            if (p.state == PlayerState.ATTACK) {
                paint.color = Color.WHITE
                val wx = if (p.facing>0) x+w else x-24f
                canvas.drawRect(wx, y+12f, wx+24f, y+18f, paint)
            }
        }

        // HP bar above player if damaged
        if (p.hp < p.maxHp) {
            val pct = p.hp.toFloat()/p.maxHp
            paint.color = Color.argb(200, 40,40,40)
            canvas.drawRect(x-6f, y-14f, x+w+6f, y-6f, paint)
            paint.color = Color.parseColor("#F87171")
            canvas.drawRect(x-6f, y-14f, x-6f + (w+12f)*pct, y-6f, paint)
        }
    }

    private fun renderEnemy(canvas: Canvas, e: Enemy) {
        val x = e.pos.x
        val y = e.pos.y
        val w = e.width
        val h = e.height

        // shadow
        paint.color = Color.argb(50,0,0,0)
        canvas.drawOval(x-2f, y+h-4f, x+w+2f, y+h+4f, paint)

        val baseColor = when(e.archetype) {
            EnemyArchetype.SHAMBLER_HUSK -> Color.parseColor("#8A8A8A")
            EnemyArchetype.THORNLING -> Color.parseColor("#7FB069")
            EnemyArchetype.GLOOM_MOTH -> Color.parseColor("#9A8AA0")
            EnemyArchetype.AEGIS_REMNANT -> Color.parseColor("#5A6A7A")
            EnemyArchetype.CINDER_CHARGER -> Color.parseColor("#8B2500")
            EnemyArchetype.RIFT_STALKER -> Color.parseColor("#6A5ACD")
            EnemyArchetype.SORROW_SOWER -> Color.parseColor("#4A2A6A")
            EnemyArchetype.SPIRE_WARDEN -> Color.parseColor("#8B7355")
            else -> Color.GRAY
        }

        paint.color = if (e.isElite) adjustColor(baseColor, 1.4f) else baseColor
        if (e.state == EnemyState.HURT && (System.currentTimeMillis()/60 % 2L == 0L)) {
            paint.color = Color.WHITE
        }

        // simple shape per type
        when(e.archetype) {
            EnemyArchetype.GLOOM_MOTH -> {
                canvas.drawOval(x, y, x+w, y+h, paint)
                // wings
                paint.color = adjustColor(baseColor, 0.7f)
                canvas.drawOval(x-8f, y+2f, x+10f, y+h-2f, paint)
                canvas.drawOval(x+w-10f, y+2f, x+w+8f, y+h-2f, paint)
            }
            EnemyArchetype.AEGIS_REMNANT -> {
                canvas.drawRect(x, y, x+w, y+h, paint)
                // shield
                if ((e as AegisRemnant).isBlocking) {
                    paint.color = Color.parseColor("#7DE2FF")
                    val sx = if (e.facing>0) x+w else x-12f
                    canvas.drawRect(sx, y, sx+12f, y+h, paint)
                }
            }
            else -> {
                canvas.drawRect(x, y, x+w, y+h, paint)
                // eyes
                paint.color = Color.parseColor("#FF6B6B")
                val eyeX = if (e.facing>0) x+w-10f else x+2f
                canvas.drawRect(eyeX, y+6f, eyeX+6f, y+10f, paint)
            }
        }

        if (e.isElite) {
            // elite aura
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            paint.color = Color.parseColor("#FFD700")
            canvas.drawRect(x-4f, y-4f, x+w+4f, y+h+4f, paint)
            paint.style = Paint.Style.FILL
        }

        // hp bar
        if (e.hp < e.maxHp) {
            val pct = e.hp.toFloat()/e.maxHp
            paint.color = Color.argb(180, 20,20,20)
            canvas.drawRect(x, y-10f, x+w, y-4f, paint)
            paint.color = if (e.isElite) Color.parseColor("#FFD700") else Color.parseColor("#F87171")
            canvas.drawRect(x, y-10f, x+w*pct, y-4f, paint)
        }
    }

    private fun renderBoss(canvas: Canvas, b: Boss) {
        val x = b.pos.x
        val y = b.pos.y
        val w = b.width
        val h = b.height

        paint.color = Color.argb(80,0,0,0)
        canvas.drawOval(x-10f, y+h-8f, x+w+10f, y+h+12f, paint)

        val col = when(b.bossType) {
            com.lumenfall.echoes.entities.BossType.WARDEN_OF_ASH -> Color.parseColor("#8A8A8A")
            com.lumenfall.echoes.entities.BossType.MATRIARCH_OF_ROOTS -> Color.parseColor("#2D4A22")
            com.lumenfall.echoes.entities.BossType.FORGEHEART_COLOSSUS -> Color.parseColor("#8B2500")
            else -> Color.parseColor("#4A2A6A")
        }
        paint.color = col
        if (b.state == EnemyState.HURT && System.currentTimeMillis()/80 % 2L ==0L) paint.color = Color.WHITE
        canvas.drawRect(x, y, x+w, y+h, paint)

        // boss details
        paint.color = Color.parseColor("#FFC857")
        canvas.drawRect(x+w*0.2f, y+10f, x+w*0.8f, y+20f, paint)

        // big hp bar at top of screen? We'll draw above boss too
        val pct = b.hp.toFloat()/b.maxHp
        paint.color = Color.argb(200, 0,0,0)
        canvas.drawRect(x, y-18f, x+w, y-6f, paint)
        paint.color = Color.parseColor("#FF6B6B")
        canvas.drawRect(x, y-18f, x+w*pct, y-6f, paint)

        // name
        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(b.bossType.name.replace("_"," "), x+w/2, y-24f, paint)
    }

    private fun renderHUD(canvas: Canvas) {
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()

        // Top bar
        hudPaint.color = Color.argb(160, 18,18,30)
        canvas.drawRect(0f,0f,w,110f, hudPaint)

        // HP
        val hpPct = player.hp.toFloat()/player.maxHp
        hudPaint.color = Color.argb(200, 40,40,40)
        canvas.drawRoundRect(20f,20f,320f,48f, 12f,12f, hudPaint)
        hudPaint.color = Color.parseColor("#F87171")
        canvas.drawRoundRect(20f,20f,20f+300f*hpPct,48f,12f,12f, hudPaint)
        textPaint.color = Color.WHITE
        textPaint.textSize = 22f
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("HP ${player.hp}/${player.maxHp}", 30f, 40f, textPaint)

        // Coins
        hudPaint.color = Color.parseColor("#FFD700")
        canvas.drawCircle(400f, 34f, 12f, hudPaint)
        textPaint.color = Color.WHITE
        canvas.drawText("${player.coins}  (${runManager?.coins ?: 0})", 424f, 42f, textPaint)

        // Weapon
        textPaint.textSize = 20f
        canvas.drawText("W: ${player.primaryWeapon.name}", 20f, 80f, textPaint)
        canvas.drawText("A1: ${player.ability1.name} [${if(player.ability1.cooldownTimer>0) "${player.ability1.cooldownTimer.toInt()}s" else "READY"}]  A2: ${player.ability2.name}", 20f, 102f, textPaint)

        // Biome info
        currentRoom?.let {
            val biome = BiomeRegistry.get(it.biome)
            textPaint.textAlign = Paint.Align.RIGHT
            textPaint.color = Color.parseColor("#9AA0B0")
            canvas.drawText("${biome.name} - Room ${runManager?.currentRoomIndex ?: 0}/${runManager?.getCurrentBiomeRun()?.rooms?.size ?: 0}", w-20f, 40f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT
        }

        // Minimap hint
        if (currentRoom?.isCleared == true) {
            textPaint.color = Color.parseColor("#4ADE80")
            textPaint.textSize = 24f
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("Room Cleared! Find EXIT →", w/2, 140f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT
        }
    }

    private fun renderTouchControls(canvas: Canvas, controls: TouchControls) {
        // Joystick
        val joy = controls.joystick
        paint.color = Color.argb((joy.alpha*80).toInt(), 255,255,255)
        canvas.drawCircle(joy.centerX, joy.centerY, joy.radius, paint)
        paint.color = Color.argb((joy.alpha*180).toInt(), 255,255,255)
        canvas.drawCircle(joy.thumbX, joy.thumbY, joy.thumbRadius, paint)

        // Buttons
        for (btn in controls.buttons.values) {
            val alpha = (btn.alpha*255).toInt()
            paint.color = if (btn.isPressed) Color.argb(alpha, 125,226,255) else Color.argb((alpha*0.6f).toInt(), 255,255,255)
            paint.style = Paint.Style.FILL
            // rounded rect
            canvas.drawRoundRect(btn.rect, 24f*btn.scale, 24f*btn.scale, paint)
            // label
            paint.color = Color.argb(alpha, 0,0,0)
            paint.textSize = 22f*btn.scale
            paint.textAlign = Paint.Align.CENTER
            val label = when(btn.id) {
                "attack" -> "ATK"
                "jump" -> "JMP"
                "dash" -> "DSH"
                "ability1" -> "A1"
                "ability2" -> "A2"
                "interact" -> "USE"
                "pause" -> "||"
                else -> btn.id
            }
            canvas.drawText(label, btn.rect.centerX(), btn.rect.centerY()+8f, paint)
        }
    }

    private fun renderDeath(canvas: Canvas) {
        renderGame(canvas)
        paint.color = Color.argb((min(200f, deathTimer*120f)).toInt(), 0,0,0)
        canvas.drawRect(0f,0f,canvas.width.toFloat(), canvas.height.toFloat(), paint)
        paint.color = Color.WHITE
        paint.textSize = 72f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("ECHO FADES", canvas.width/2f, canvas.height/2f - 40f, paint)
        paint.textSize = 28f
        paint.color = Color.parseColor("#9AA0B0")
        canvas.drawText("The Citadel shifts. You will return.", canvas.width/2f, canvas.height/2f + 20f, paint)
        paint.textSize = 22f
        canvas.drawText("Rooms: ${runManager?.runStats?.roomsCleared ?: 0}  Coins: ${runManager?.coins ?: 0}  Kills: ${runManager?.runStats?.enemiesKilled ?: 0}", canvas.width/2f, canvas.height/2f + 60f, paint)
    }

    private fun renderVictory(canvas: Canvas) {
        canvas.drawColor(Color.parseColor("#0A0A12"))
        paint.color = Color.parseColor("#FFD700")
        paint.textSize = 80f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("CITADEL CLAIMED", canvas.width/2f, canvas.height/2f - 20f, paint)
        paint.color = Color.WHITE
        paint.textSize = 28f
        canvas.drawText("You have broken the cycle... for now.", canvas.width/2f, canvas.height/2f + 30f, paint)
    }

    private fun adjustColor(color: Int, factor: Float): Int {
        val a = Color.alpha(color)
        val r = (Color.red(color)*factor).toInt().coerceIn(0,255)
        val g = (Color.green(color)*factor).toInt().coerceIn(0,255)
        val b = (Color.blue(color)*factor).toInt().coerceIn(0,255)
        return Color.argb(a,r,g,b)
    }

    // Input handling - called from MainActivity
    fun handleTouch(event: android.view.MotionEvent): Boolean {
        if (gameState == GameState.MAIN_MENU) {
            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                // simple: any tap starts new run, or menu selection via vertical swipe?
                // For simplicity, tap to start
                startNewRun()
                return true
            }
        } else if (gameState == GameState.PAUSED) {
            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                gameState = GameState.PLAYING
                return true
            }
        } else if (gameState == GameState.DEATH || gameState == GameState.VICTORY) {
            if (event.action == android.view.MotionEvent.ACTION_DOWN && deathTimer>1f) {
                gameState = GameState.MAIN_MENU
                return true
            }
        }
        return touchControls?.onTouchEvent(event) ?: false
    }

    // Game thread
    private class GameThread(
        private val holder: SurfaceHolder,
        private val engine: GameEngine
    ) : Thread() {
        override fun run() {
            var lastTime = System.nanoTime()
            var accumulator = 0f
            val dt = Constants.FIXED_DT

            while (engine.isRunning) {
                val now = System.nanoTime()
                var frameTime = (now - lastTime) / 1_000_000_000f
                lastTime = now
                if (frameTime > 0.25f) frameTime = 0.25f
                accumulator += frameTime

                while (accumulator >= dt) {
                    engine.update(dt)
                    accumulator -= dt
                }

                var canvas: Canvas? = null
                try {
                    canvas = holder.lockCanvas()
                    if (canvas != null) {
                        synchronized(holder) {
                            engine.render(canvas)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("GameThread", "Render error", e)
                } finally {
                    if (canvas != null) {
                        try { holder.unlockCanvasAndPost(canvas) } catch (e: Exception) {}
                    }
                }

                // cap FPS
                val sleepTime = (Constants.FRAME_TIME_MS - (System.nanoTime()-now)/1_000_000).coerceAtLeast(0)
                try { sleep(sleepTime) } catch (e: Exception) {}
            }
        }
    }

    class Camera {
        var x = 0f
        var y = 0f
        var targetX = 0f
        var targetY = 0f
        var screenW = 1920f
        var screenH = 1080f
        var shakeX = 0f
        var shakeY = 0f

        fun setScreenSize(w: Float, h: Float) {
            screenW = w
            screenH = h
        }

        fun follow(entity: Entity, instant: Boolean) {
            targetX = entity.center.x - screenW/2
            targetY = entity.center.y - screenH/2
            if (instant) {
                x = targetX
                y = targetY
            }
        }

        fun update(dt: Float, shakeIntensity: Float) {
            // smooth follow
            x += (targetX - x) * 5f * dt
            y += (targetY - y) * 5f * dt
            // clamp to room? handled outside

            // shake
            if (shakeIntensity > 0f) {
                shakeX = (Math.random().toFloat()-0.5f)*2f*shakeIntensity
                shakeY = (Math.random().toFloat()-0.5f)*2f*shakeIntensity
                x += shakeX
                y += shakeY
            }

            // clamp y to not go too far
            y = y.coerceAtMost(200f)
        }
    }
}
