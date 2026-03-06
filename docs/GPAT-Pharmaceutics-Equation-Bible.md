---
title: "GPAT Pharmaceutics Equation Bible"
subtitle: "Complete Formula Handbook — B.Pharm Sem 4–6"
author: "GPAT Revision Series"
date: "2026"
subject: "Pharmaceutics"
keywords: [GPAT, Pharmaceutics, Biopharmaceutics, Pharmacokinetics, Equations, Formulas]
lang: en
toc: true
toc-depth: 3
numbersections: true
geometry: "margin=2.5cm"
fontsize: 11pt
linestretch: 1.4
colorlinks: true
linkcolor: NavyBlue
urlcolor: NavyBlue
header-includes: |
  \usepackage{booktabs}
  \usepackage{longtable}
  \usepackage{amsmath}
  \usepackage{amssymb}
  \usepackage{fancyhdr}
  \pagestyle{fancy}
  \fancyhead[L]{\small GPAT Pharmaceutics Equation Bible}
  \fancyhead[R]{\small B.Pharm Sem 4–6}
  \fancyfoot[C]{\thepage}
---

\newpage

---

> **How to Use This Handbook**
> Each section presents equations in the order they appear most frequently in GPAT papers.
> Highlighted boxes call out "trap questions" — variants that catch students who only memorise
> the basic form. Work through the rapid-revision tables at the end of each chapter before
> attempting practice questions.

---

\newpage

# Biopharmaceutics & Pharmacokinetics

## Core Pharmacokinetic Parameters

### Half-Life

$$t_{1/2} = \frac{0.693}{k_e}$$

| Symbol | Meaning |
|--------|---------|
| $t_{1/2}$ | Elimination half-life |
| $k_e$ | First-order elimination rate constant (h⁻¹) |

> **GPAT Trap:** For a two-compartment model the *terminal* half-life uses the $\beta$ rate constant:
> $t_{1/2(\beta)} = 0.693/\beta$. Do **not** confuse $k_e$ with $k_{12}$ or $k_{21}$.

---

### Volume of Distribution (V~d~)

$$V_d = \frac{\text{Dose}}{C_0}$$

$$V_d = \frac{\text{Dose} \times F}{AUC \times k_e}$$

| Symbol | Meaning |
|--------|---------|
| $C_0$ | Initial plasma concentration |
| $F$ | Bioavailability fraction (0–1) |
| AUC | Area under the curve |

---

### Clearance

$$CL = k_e \times V_d$$

$$CL = \frac{\text{Dose} \times F}{AUC}$$

$$CL_{total} = CL_{renal} + CL_{hepatic} + CL_{other}$$

---

### Area Under the Curve (AUC)

**Trapezoidal rule:**

$$AUC_{0 \to t} = \sum_{i=1}^{n} \frac{(C_i + C_{i-1})}{2} \cdot (t_i - t_{i-1})$$

**Extrapolation to infinity:**

$$AUC_{0 \to \infty} = AUC_{0 \to t} + \frac{C_{last}}{k_e}$$

---

### Bioavailability

**Absolute bioavailability:**

$$F = \frac{AUC_{oral} \times \text{Dose}_{IV}}{AUC_{IV} \times \text{Dose}_{oral}}$$

**Relative bioavailability:**

$$F_{rel} = \frac{AUC_{test}}{AUC_{reference}}$$

---

## One-Compartment Open Model (IV Bolus)

$$C_t = C_0 \cdot e^{-k_e t}$$

$$\ln C_t = \ln C_0 - k_e \cdot t$$

**Slope of log-linear plot** $= -k_e / 2.303$ (when using log₁₀)

---

## One-Compartment Open Model (Oral/Extravascular)

$$C_t = \frac{F \cdot D \cdot k_a}{V_d (k_a - k_e)} \left(e^{-k_e t} - e^{-k_a t}\right)$$

| Symbol | Meaning |
|--------|---------|
| $k_a$ | Absorption rate constant |
| $D$ | Dose |

**Time to peak concentration (T~max~):**

$$T_{max} = \frac{\ln(k_a/k_e)}{k_a - k_e}$$

**Peak concentration (C~max~):**

$$C_{max} = \frac{F \cdot D}{V_d} \cdot e^{-k_e T_{max}}$$

---

## Two-Compartment Model

$$C_t = A \cdot e^{-\alpha t} + B \cdot e^{-\beta t}$$

| Symbol | Meaning |
|--------|---------|
| $A, B$ | Hybrid constants (intercepts) |
| $\alpha$ | Distribution rate constant |
| $\beta$ | Elimination rate constant |

$$\alpha + \beta = k_{12} + k_{21} + k_e$$

$$\alpha \cdot \beta = k_{21} \cdot k_e$$

---

## Multiple Dosing — Accumulation

**Accumulation factor (R):**

$$R = \frac{1}{1 - e^{-k_e \tau}}$$

**Steady-state minimum (trough) concentration:**

$$C_{ss,min} = \frac{C_0 \cdot e^{-k_e \tau}}{1 - e^{-k_e \tau}}$$

**Steady-state maximum (peak) concentration:**

$$C_{ss,max} = \frac{C_0}{1 - e^{-k_e \tau}}$$

| Symbol | Meaning |
|--------|---------|
| $\tau$ | Dosing interval |

> **GPAT Trap:** Accumulation factor is *not* the number of doses to reach steady state.
> Steady state is reached in ~4–5 half-lives regardless of dosing interval.

---

## Renal Clearance

$$CL_R = \frac{A_u}{\text{AUC}_{0 \to \infty}}$$

$$CL_R = GFR \cdot f_u \pm \text{secretion} - \text{reabsorption}$$

| Symbol | Meaning |
|--------|---------|
| $A_u$ | Amount excreted unchanged in urine |
| $f_u$ | Free (unbound) fraction in plasma |
| GFR | Glomerular filtration rate (~125 mL/min normal) |

---

## Hepatic Clearance & First-Pass Effect

**Well-stirred (venous equilibrium) model:**

$$CL_H = \frac{Q_H \cdot f_u \cdot CL_{int}}{Q_H + f_u \cdot CL_{int}}$$

**Extraction ratio (E):**

$$E = \frac{CL_H}{Q_H}$$

**Oral bioavailability (first-pass):**

$$F = (1 - E) = 1 - \frac{CL_H}{Q_H}$$

| Symbol | Meaning |
|--------|---------|
| $Q_H$ | Hepatic blood flow (~1500 mL/min) |
| $CL_{int}$ | Intrinsic clearance |

---

## Rapid Revision Table — PK Parameters

| Parameter | Formula | Unit |
|-----------|---------|------|
| $k_e$ | $0.693 / t_{1/2}$ | h⁻¹ |
| $t_{1/2}$ | $0.693 / k_e$ | h |
| $V_d$ | Dose / $C_0$ | L or L/kg |
| $CL$ | $k_e \times V_d$ | L/h |
| $AUC_{IV}$ | $C_0 / k_e$ | mg·h/L |
| $T_{max}$ | $\ln(k_a/k_e)/(k_a - k_e)$ | h |
| $R$ (accumulation) | $1/(1-e^{-k_e\tau})$ | dimensionless |

\newpage

---

# Dissolution & Drug Release

## Noyes-Whitney Equation

$$\frac{dC}{dt} = \frac{D \cdot A}{h} \cdot (C_s - C_t)$$

**Simplified (sink conditions, C~t~ ≈ 0):**

$$\frac{dC}{dt} = \frac{D \cdot A \cdot C_s}{h}$$

| Symbol | Meaning |
|--------|---------|
| $D$ | Diffusion coefficient of drug in dissolution medium |
| $A$ | Surface area of dissolving solid |
| $h$ | Thickness of diffusion layer |
| $C_s$ | Saturation solubility |
| $C_t$ | Concentration in bulk at time $t$ |

> **GPAT Trap:** Increasing particle size *decreases* surface area (A) and *decreases* dissolution rate. Micronisation increases A, improving dissolution.

---

## Higuchi Equation (Matrix Drug Release)

$$Q = \sqrt{D(2A - C_s) C_s \cdot t}$$

**When A >> C~s~ (common assumption):**

$$Q \approx \sqrt{2 D A C_s \cdot t}$$

$$Q = k_H \sqrt{t}$$

| Symbol | Meaning |
|--------|---------|
| $Q$ | Amount of drug released per unit area at time $t$ |
| $A$ | Total amount of drug in matrix per unit volume |
| $C_s$ | Drug solubility in matrix |
| $k_H$ | Higuchi dissolution constant |

**Characteristic:** Linear plot of Q vs √t → confirms Higuchi (matrix diffusion) release.

---

## Korsmeyer-Peppas Power Law

$$\frac{M_t}{M_\infty} = k \cdot t^n$$

$$\ln\left(\frac{M_t}{M_\infty}\right) = \ln k + n \ln t$$

| $n$ value | Release mechanism | Geometry |
|-----------|-----------------|---------|
| 0.45 | Fickian diffusion | Cylinder |
| 0.45 < n < 0.89 | Anomalous (non-Fickian) | Cylinder |
| 0.89 | Case-II transport (zero-order) | Cylinder |
| 0.5 | Fickian diffusion | Slab |
| 1.0 | Zero-order | Slab |

> **GPAT Trap:** n = 0.5 indicates Fickian diffusion for a **slab**; n = 0.45 for a **cylinder**. Examiners often mix geometries.

---

## Zero-Order Release

$$Q_t = Q_0 + k_0 t$$

Plot: $Q_t$ vs $t$ — straight line; slope = $k_0$ (release rate constant).

---

## First-Order Release

$$\ln Q_t = \ln Q_0 - k_1 t$$

$$Q_t = Q_0 \cdot e^{-k_1 t}$$

---

## Hixson-Crowell Cube Root Law

$$W_0^{1/3} - W_t^{1/3} = k_{HC} \cdot t$$

| Symbol | Meaning |
|--------|---------|
| $W_0$ | Initial weight of particle |
| $W_t$ | Weight at time $t$ |
| $k_{HC}$ | Hixson-Crowell dissolution rate constant |

**Indicates:** Dissolution occurs perpendicular to surface with change in particle size/surface area over time.

---

## Dissolution Efficiency (DE)

$$DE = \frac{\int_0^t y \cdot dt}{y_{100} \cdot t} \times 100\%$$

| Symbol | Meaning |
|--------|---------|
| $y$ | % drug dissolved at time $t$ |
| $y_{100}$ | 100% dissolution |

---

## Rapid Revision Table — Dissolution Models

| Model | Plot | Slope | Mechanism |
|-------|------|-------|-----------|
| Zero-order | Q vs t | $k_0$ | Constant rate |
| First-order | ln Q vs t | $-k_1$ | Concentration-dependent |
| Higuchi | Q vs √t | $k_H$ | Matrix diffusion |
| Hixson-Crowell | $W^{1/3}$ vs t | $k_{HC}$ | Surface erosion |
| Korsmeyer-Peppas | ln(M/M∞) vs ln t | $n$ | Various |

\newpage

---

# Diffusion, Permeation & Membrane Transport

## Fick's First Law of Diffusion

$$J = -D \frac{dC}{dx}$$

**Across membrane (steady-state):**

$$J = D \frac{(C_1 - C_2)}{h} = P \cdot \Delta C$$

| Symbol | Meaning |
|--------|---------|
| $J$ | Flux (amount/area/time) |
| $D$ | Diffusion coefficient (cm²/s) |
| $dC/dx$ | Concentration gradient |
| $h$ | Membrane thickness |
| $P$ | Permeability coefficient = $D/h$ |

---

## Fick's Second Law

$$\frac{\partial C}{\partial t} = D \frac{\partial^2 C}{\partial x^2}$$

---

## Permeability Coefficient

$$P = \frac{D \cdot K}{h}$$

| Symbol | Meaning |
|--------|---------|
| $K$ | Partition coefficient (membrane/donor) |

---

## Stokes-Einstein Equation

$$D = \frac{k_B T}{6 \pi \eta r}$$

| Symbol | Meaning |
|--------|---------|
| $k_B$ | Boltzmann constant (1.38 × 10⁻²³ J/K) |
| $T$ | Absolute temperature (K) |
| $\eta$ | Viscosity of medium |
| $r$ | Hydrodynamic radius of solute |

---

## Henderson-Hasselbalch Equations

**For weak acids:**

$$\text{pH} = \text{p}K_a + \log\frac{[\text{A}^-]}{[\text{HA}]}$$

**For weak bases:**

$$\text{pH} = \text{p}K_a + \log\frac{[\text{B}]}{[\text{BH}^+]}$$

**% Ionised (weak acid):**

$$\% \text{ ionised} = \frac{100}{1 + 10^{(\text{p}K_a - \text{pH})}}$$

**% Ionised (weak base):**

$$\% \text{ ionised} = \frac{100}{1 + 10^{(\text{pH} - \text{p}K_a)}}$$

> **GPAT Trap:** Absorption favours the **un-ionised** form. A weak acid (aspirin, pKa 3.5) is predominantly un-ionised in stomach (pH 1.2) → favourable absorption there.

---

## Partition Coefficient

$$K_{o/w} = \frac{[\text{drug}]_{octanol}}{[\text{drug}]_{water}}$$

$$\log P = \log K_{o/w}$$

**Distribution coefficient (at specific pH):**

$$D = \frac{K_{o/w}}{1 + 10^{(\text{pH} - \text{p}K_a)}} \quad \text{(weak acid)}$$

---

## Rapid Revision Table — Diffusion & Permeation

| Parameter | Key Relationship |
|-----------|----------------|
| Flux ($J$) | ↑ with ↑D, ↑ΔC; ↓ with ↑h |
| Permeability ($P$) | $D \cdot K / h$ |
| Diffusion coeff ($D$) | ↑ with ↑T, ↓ with ↑MW, ↓ with ↑η |
| Absorption | Favours un-ionised, lipophilic form |
| log P optimal range | 1–3 for good oral absorption |

\newpage

---

# Rheology

## Newton's Law of Viscosity

$$\tau = \eta \cdot \dot{\gamma}$$

| Symbol | Meaning |
|--------|---------|
| $\tau$ | Shear stress (Pa or dyne/cm²) |
| $\eta$ | Dynamic viscosity (Pa·s or poise) |
| $\dot{\gamma}$ | Shear rate (s⁻¹) |

**Kinematic viscosity:**

$$\nu = \frac{\eta}{\rho}$$

---

## Viscosity Units Conversion

| Unit | Equivalent |
|------|-----------|
| 1 Pa·s | 10 poise (P) |
| 1 mPa·s | 1 centipoise (cP) |
| 1 mPa·s | 1 cP ≈ water viscosity at 20°C |

---

## Flow Behaviour Index (Power Law / Ostwald-de Waele)

$$\tau = K \dot{\gamma}^n$$

| $n$ | Fluid type |
|-----|-----------|
| $n = 1$ | Newtonian |
| $n < 1$ | Pseudoplastic (shear-thinning) |
| $n > 1$ | Dilatant (shear-thickening) |

---

## Bingham Plastic

$$\tau = \tau_0 + \eta_p \dot{\gamma}$$

| Symbol | Meaning |
|--------|---------|
| $\tau_0$ | Yield value (minimum stress to initiate flow) |
| $\eta_p$ | Plastic viscosity |

> **GPAT Trap:** Bingham plastics have a **yield value** — they behave as solids below $\tau_0$. Examples: toothpaste, some gels, flocculated suspensions.

---

## Poiseuille's Law (Capillary Viscometry)

$$\eta = \frac{\pi r^4 \Delta P \cdot t}{8 V L}$$

**Relative viscosity (U-tube viscometer):**

$$\frac{\eta_1}{\eta_2} = \frac{\rho_1 t_1}{\rho_2 t_2}$$

---

## Thixotropy

- **Thixotropic:** Shear-thinning with *time-dependent* recovery (gel → sol → gel on standing)
- **Antithixotropic (Rheopectic):** Viscosity *increases* with time under constant shear

---

## Rapid Revision Table — Rheology

| Flow type | Yield value | n | Example |
|-----------|------------|---|---------|
| Newtonian | No | 1 | Water, glycerin |
| Pseudoplastic | No | <1 | CMC solutions, gums |
| Dilatant | No | >1 | Concentrated starch |
| Bingham plastic | Yes | 1 | Toothpaste, pastes |
| Casson | Yes | <1 | Blood at low shear |

\newpage

---

# Surface Chemistry & Colloids

## Surface/Interfacial Tension

$$W_{adhesion} = \gamma_1 + \gamma_2 - \gamma_{12}$$

$$W_{cohesion} = 2\gamma$$

---

## Young's Equation (Contact Angle)

$$\gamma_{SV} = \gamma_{SL} + \gamma_{LV} \cos\theta$$

$$\cos\theta = \frac{\gamma_{SV} - \gamma_{SL}}{\gamma_{LV}}$$

| $\theta$ | Wettability |
|---------|------------|
| $\theta = 0°$ | Complete wetting |
| $0° < \theta < 90°$ | Hydrophilic (wettable) |
| $\theta > 90°$ | Hydrophobic (non-wettable) |

---

## Spreading Coefficient

$$S = \gamma_B - \gamma_A - \gamma_{AB}$$

- $S > 0$: Spreading occurs
- $S < 0$: No spreading

---

## HLB (Hydrophile-Lipophile Balance)

**Griffin's method (non-ionic surfactants):**

$$HLB = 20 \times \frac{M_H}{M}$$

| HLB range | Application |
|----------|------------|
| 1–3 | Antifoaming agents |
| 3–6 | W/O emulsifiers |
| 7–9 | Wetting agents |
| 8–16 | O/W emulsifiers |
| 13–15 | Detergents |
| 15–18 | Solubilising agents |

**Required HLB (emulsion formulation):**

$$HLB_{blend} = \frac{f_A \cdot HLB_A + f_B \cdot HLB_B}{f_A + f_B}$$

---

## Critical Micelle Concentration (CMC)

- Below CMC: surfactant as monomer
- Above CMC: micelles form
- Detected by: break in surface tension vs log[C] plot; conductivity (ionic surfactants)

---

## Ostwald Ripening

$$\ln\frac{C_r}{C_\infty} = \frac{2\gamma V_m}{rRT}$$

| Symbol | Meaning |
|--------|---------|
| $C_r$ | Solubility of particle of radius $r$ |
| $C_\infty$ | Solubility of flat surface |
| $V_m$ | Molar volume |

---

## Zeta Potential & DLVO Theory

$$\psi_\delta = \zeta \cdot e^{-\kappa x}$$

**Stability:**
- |ζ| > 30 mV: Good colloidal stability
- |ζ| > 60 mV: Excellent stability

---

## Stokes' Law (Sedimentation)

$$v = \frac{2r^2(\rho_s - \rho_l)g}{9\eta}$$

| Symbol | Meaning |
|--------|---------|
| $v$ | Terminal settling velocity |
| $r$ | Particle radius |
| $\rho_s$ | Density of particle |
| $\rho_l$ | Density of liquid |
| $g$ | Gravitational acceleration |
| $\eta$ | Viscosity of liquid |

> **GPAT Trap:** To *reduce* sedimentation: decrease particle size, increase viscosity, match densities. Stokes' law applies only to spheres in dilute systems (laminar flow).

---

## Rapid Revision Table — Colloids & Emulsions

| Property | Formula/Value | Significance |
|---------|--------------|-------------|
| Zeta potential | > ±30 mV | Stable dispersion |
| HLB for O/W | 8–16 | Emulsifier selection |
| HLB for W/O | 3–6 | Emulsifier selection |
| CMC | Break in σ vs log C | Surfactant efficiency |
| Stokes velocity | $2r^2\Delta\rho g/9\eta$ | Sedimentation rate |

\newpage

---

# Physical Pharmacy – Thermodynamics & Solutions

## Raoult's Law

$$p_A = x_A \cdot p_A^*$$

| Symbol | Meaning |
|--------|---------|
| $p_A$ | Partial vapour pressure of component A |
| $x_A$ | Mole fraction of A in solution |
| $p_A^*$ | Vapour pressure of pure A |

---

## Colligative Properties

**Boiling point elevation:**

$$\Delta T_b = i \cdot K_b \cdot m$$

**Freezing point depression:**

$$\Delta T_f = i \cdot K_f \cdot m$$

**Osmotic pressure:**

$$\pi = i \cdot M \cdot R \cdot T$$

| Symbol | Meaning |
|--------|---------|
| $i$ | Van't Hoff factor (no. of ions) |
| $K_b$ | Ebullioscopic constant (water: 0.512 °C·kg/mol) |
| $K_f$ | Cryoscopic constant (water: 1.86 °C·kg/mol) |
| $m$ | Molality (mol/kg) |
| $M$ | Molarity (mol/L) |
| $R$ | Gas constant (0.082 L·atm/mol·K) |

**Isotonicity:** Blood serum $\Delta T_f = -0.52°C$ (equivalent to 0.9% NaCl)

---

## Solubility & Thermodynamics

**Ideal solubility:**

$$\log x_2 = \frac{\Delta H_f}{2.303 R}\left(\frac{1}{T_m} - \frac{1}{T}\right)$$

**Hildebrand solubility parameter:**

$$\delta = \left(\frac{\Delta E_{vap}}{V}\right)^{1/2}$$

"Like dissolves like": solute and solvent with similar $\delta$ values → high solubility.

---

## Arrhenius Equation (Stability/Reaction Kinetics)

$$k = A \cdot e^{-E_a/RT}$$

$$\ln k = \ln A - \frac{E_a}{RT}$$

$$\log k = \log A - \frac{E_a}{2.303 RT}$$

| Symbol | Meaning |
|--------|---------|
| $k$ | Rate constant |
| $A$ | Frequency (pre-exponential) factor |
| $E_a$ | Activation energy (J/mol) |
| $R$ | Gas constant (8.314 J/mol·K) |
| $T$ | Absolute temperature (K) |

**Accelerated stability — two temperatures:**

$$\frac{k_2}{k_1} = e^{\frac{E_a}{R}\left(\frac{1}{T_1}-\frac{1}{T_2}\right)}$$

---

## Q₁₀ Concept

$$Q_{10} = \frac{k_{T+10}}{k_T}$$

Typical pharmaceutical value: $Q_{10} \approx 2$ to $3$

$$t_{90}(T_1) = t_{90}(T_2) \times Q_{10}^{(T_2-T_1)/10}$$

> **GPAT Trap:** Q₁₀ = 2 means rate *doubles* per 10°C rise in temperature. A product stable for 2 years at 4°C may only be stable for 6 months at 25°C if Q₁₀ = 2.

---

## Reaction Orders — Half-Life Summary

| Order | Integrated rate law | Half-life |
|-------|-------------------|---------|
| Zero | $C = C_0 - kt$ | $t_{1/2} = C_0/2k$ |
| First | $\ln C = \ln C_0 - kt$ | $t_{1/2} = 0.693/k$ |
| Second | $1/C = 1/C_0 + kt$ | $t_{1/2} = 1/kC_0$ |

> **GPAT Trap:** Only first-order half-life is *independent* of initial concentration.

---

## Rapid Revision Table — Stability & Kinetics

| Concept | Key formula | GPAT value |
|---------|------------|-----------|
| Arrhenius | $k = Ae^{-Ea/RT}$ | Shelf-life prediction |
| Q₁₀ | Rate doubles per 10°C | Typically Q₁₀ = 2–3 |
| Zero-order $t_{1/2}$ | $C_0/2k$ | Concentration-dependent |
| First-order $t_{1/2}$ | $0.693/k$ | Concentration-independent |
| $t_{90}$ | 90% remaining; $0.105/k$ (1st order) | Expiry dating |

\newpage

---

# Particle Science & Powder Technology

## Particle Size Distribution

**Arithmetic mean diameter:**

$$\bar{d} = \frac{\sum n_i d_i}{\sum n_i}$$

**Surface-volume (Sauter) mean diameter:**

$$d_{sv} = \frac{\sum n_i d_i^3}{\sum n_i d_i^2}$$

---

## Specific Surface Area

$$S_w = \frac{6}{\rho \cdot d_{sv}}$$

$$S_v = \frac{6}{d_{sv}}$$

| Symbol | Meaning |
|--------|---------|
| $S_w$ | Specific surface area (per unit mass) |
| $S_v$ | Specific surface area (per unit volume) |
| $\rho$ | Particle density |

---

## Carr's Index (Compressibility Index)

$$CI = \frac{\rho_{tapped} - \rho_{bulk}}{\rho_{tapped}} \times 100$$

| CI (%) | Flow character |
|--------|--------------|
| 1–10 | Excellent |
| 11–15 | Good |
| 16–20 | Fair |
| 21–25 | Passable |
| 26–31 | Poor |
| 32–37 | Very poor |
| >38 | Extremely poor |

---

## Hausner Ratio

$$HR = \frac{\rho_{tapped}}{\rho_{bulk}}$$

| HR | Flow character |
|----|--------------|
| < 1.25 | Good flow |
| 1.25–1.50 | Fair |
| > 1.50 | Poor |

---

## Angle of Repose

$$\tan\theta = \frac{h}{r}$$

| $\theta$ | Flow character |
|---------|--------------|
| < 25° | Excellent |
| 25–30° | Good |
| 30–40° | Passable |
| > 40° | Very poor |

---

## BET (Brunauer-Emmett-Teller) Equation

$$\frac{p/p_0}{V(1-p/p_0)} = \frac{1}{V_m C} + \frac{C-1}{V_m C} \cdot \frac{p}{p_0}$$

Used to determine specific surface area from gas adsorption isotherms.

---

## Rapid Revision Table — Particle/Powder

| Test | Formula | Good flow threshold |
|------|---------|-------------------|
| Carr's Index | $(ρ_t-ρ_b)/ρ_t × 100$ | < 15% |
| Hausner Ratio | $ρ_t/ρ_b$ | < 1.25 |
| Angle of repose | $\tan^{-1}(h/r)$ | < 30° |
| Sauter mean | $\Sigma nd^3/\Sigma nd^2$ | — |

\newpage

---

# Dosage Form Design — Key Equations

## Tablet Compression

**Tensile strength (diametral compression):**

$$T_s = \frac{2F}{\pi D t}$$

| Symbol | Meaning |
|--------|---------|
| $F$ | Breaking force |
| $D$ | Tablet diameter |
| $t$ | Tablet thickness |

**Crushing strength (hardness):** directly measured in kP or N (no formula needed, but $T_s$ is the normalised measure).

---

## Buffer Capacity

$$\beta = \frac{d(\text{base equivalents})}{d(\text{pH})} = \frac{2.303 \cdot K_a [\text{H}^+] C}{(K_a + [\text{H}^+])^2}$$

Maximum buffer capacity occurs at **pH = pKa** (equal concentrations of acid and conjugate base).

---

## Isotonicity Calculation — Sodium Chloride Equivalent (E value)

$$\text{Mass of NaCl equivalent} = w \times E$$

$$\text{NaCl to add} = 0.9 - (w \times E) \text{ g per 100 mL}$$

---

## Parenteral Formulation — Tonicity

**Freezing point depression method:**

$$\text{Volume of iso. vehicle} = \frac{0.52 - \Delta T_{f(\text{drug})}}{0.52} \times 100 \text{ mL}$$

---

## Suspension — Flocculation Ratio (F)

$$F = \frac{V_u}{V_f}$$

| Symbol | Meaning |
|--------|---------|
| $V_u$ | Sediment volume in flocculated suspension |
| $V_f$ | Total volume of suspension |

$F = 1$: perfectly flocculated (ideal); $F < 1$: some sedimentation

---

## Emulsion — Creaming Rate (Stokes')

$$v_{cream} = \frac{2r^2(\rho_o - \rho_w)g}{9\eta}$$

Strategies to reduce creaming:
- Reduce droplet size (homogenisation)
- Match densities
- Increase continuous-phase viscosity

---

## Rapid Revision Table — Formulation Parameters

| Parameter | Formula | Optimal range |
|---------|---------|--------------|
| Tensile strength | $2F/\pi Dt$ | 1–2 MPa (typical) |
| Buffer capacity max | at pH = pKa | — |
| Flocculation ratio | $V_u/V_f$ | 1.0 (ideal) |
| Isotonicity | ΔTf = −0.52°C | 0.9% NaCl equivalent |

\newpage

---

# Novel Drug Delivery Systems (NDDS)

## Membrane-Controlled (Reservoir) Systems

**Zero-order release rate:**

$$\frac{dM}{dt} = \frac{D \cdot K \cdot A \cdot \Delta C}{h} = \text{constant}$$

---

## Osmotic Pump Systems (OROS)

**Drug release rate:**

$$\frac{dM}{dt} = \frac{dV}{dt} \cdot C_s = A \cdot \frac{L_p \cdot \sigma \cdot \Delta\pi}{h} \cdot C_s$$

| Symbol | Meaning |
|--------|---------|
| $L_p$ | Hydraulic permeability of membrane |
| $\sigma$ | Reflection coefficient |
| $\Delta\pi$ | Osmotic pressure difference |

---

## Nanoparticle Size & Surface

**Specific surface area of nanosphere:**

$$S = \frac{6}{\rho \cdot d}$$

**Encapsulation efficiency (EE):**

$$EE\% = \frac{\text{Total drug} - \text{Free drug}}{\text{Total drug}} \times 100$$

**Loading efficiency (LE):**

$$LE\% = \frac{\text{Drug in nanoparticles}}{\text{Weight of nanoparticles}} \times 100$$

---

## Transdermal Drug Delivery — Flux

$$J_{skin} = K_p \cdot C_{donor}$$

**Enhancement ratio (ER):**

$$ER = \frac{J_{enhanced}}{J_{control}}$$

---

## Liposome Lamellarity

**Unilamellar vesicles (ULV):** single bilayer membrane
**Multilamellar vesicles (MLV):** multiple concentric bilayers

**Aqueous volume per lipid (trapped volume):**

$$TV = \frac{V_{aqueous}}{n_{lipid}} \quad (\mu\text{L}/\mu\text{mol})$$

---

## Rapid Revision Table — NDDS

| System | Release mechanism | Equation |
|--------|-----------------|---------|
| Matrix | Diffusion | Higuchi: $Q = k_H\sqrt{t}$ |
| Reservoir | Membrane control | Zero-order |
| OROS | Osmosis | Proportional to $\Delta\pi$ |
| Transdermal patch | Skin permeation | $J = K_p \cdot C$ |
| Nanoparticle | Variable | EE%, LE% key parameters |

\newpage

---

# Common GPAT Trap Questions

## Trap 1 — Half-life & Compartments

**Q:** A drug follows two-compartment kinetics. The "half-life" reported on the label refers to which rate constant?

**A:** The *terminal* half-life $t_{1/2,\beta} = 0.693/\beta$, not the distribution phase ($\alpha$).

---

## Trap 2 — Zero vs First Order

**Q:** The half-life of a drug decreases as initial concentration increases. What order?

**A:** **Second order** ($t_{1/2} = 1/kC_0$). Only zero-order is longer at higher concentrations; first-order is independent.

---

## Trap 3 — Noyes-Whitney & Particle Size

**Q:** Micronisation of a BCS Class II drug improves bioavailability primarily because it:

**A:** Increases surface area (A in Noyes-Whitney), increasing dissolution rate. Solubility $C_s$ is *unchanged* by particle size at macro scale.

---

## Trap 4 — HLB for W/O vs O/W

**Q:** An emulsifier with HLB 5 is best suited for?

**A:** **W/O emulsion** (HLB 3–6). Higher HLB (8–16) = O/W.

---

## Trap 5 — Korsmeyer-Peppas n value

**Q:** A cylindrical matrix tablet gives n = 0.6 in Korsmeyer-Peppas. What does this mean?

**A:** Anomalous (non-Fickian) transport — combined diffusion and polymer relaxation. For a cylinder, n = 0.45 (Fickian), 0.89 (Case-II).

---

## Trap 6 — Accumulation vs Steady State

**Q:** How many doses are needed to reach steady-state plasma concentration?

**A:** Steady state is reached in ~**4–5 half-lives** regardless of dosing interval or dose size. Accumulation factor R tells you *how much* drug accumulates, not when steady state is reached.

---

## Trap 7 — Carr's Index Values

**Q:** A powder shows Carr's Index of 22%. Its flow would be rated as?

**A:** **Passable** (21–25%). Often confused with "poor" (26–31%).

---

## Trap 8 — Weak Acid Absorption Site

**Q:** Where is aspirin (weak acid, pKa 3.5) best absorbed?

**A:** **Stomach** (pH 1.2) — predominantly un-ionised at stomach pH (pH < pKa), favouring passive absorption. Despite this, small intestine absorbs more due to larger surface area.

---

## Trap 9 — Q₁₀ Direction

**Q:** If Q₁₀ = 3 and shelf life at 40°C is 6 months, what is estimated shelf life at 25°C?

**A:**

$$t_{25} = t_{40} \times Q_{10}^{(40-25)/10} = 6 \times 3^{1.5} \approx 6 \times 5.2 \approx 31 \text{ months}$$

---

## Trap 10 — Zeta Potential Stability

**Q:** A colloidal dispersion with zeta potential of −15 mV is considered?

**A:** **Unstable** (borderline). Only |ζ| ≥ 30 mV provides adequate electrostatic stabilisation.

\newpage

---

# Final Expected GPAT Questions

## Section A — Pharmacokinetics (High-Frequency)

1. **(2 marks)** A drug has $k_e = 0.1$ h⁻¹ and $V_d = 50$ L. Calculate $CL$, $t_{1/2}$, and $AUC_{0\to\infty}$ after a 500 mg IV bolus.

   **Solution:**
   - $CL = k_e \times V_d = 0.1 \times 50 = 5$ L/h
   - $t_{1/2} = 0.693/0.1 = 6.93$ h
   - $C_0 = 500/50 = 10$ mg/L
   - $AUC = C_0/k_e = 10/0.1 = 100$ mg·h/L

---

2. **(2 marks)** An oral dose of 250 mg gives AUC = 80 mg·h/L. An IV dose of 100 mg gives AUC = 60 mg·h/L. Calculate absolute bioavailability.

   **Solution:**

   $$F = \frac{AUC_{oral} \times D_{IV}}{AUC_{IV} \times D_{oral}} = \frac{80 \times 100}{60 \times 250} = \frac{8000}{15000} = 0.533 = 53.3\%$$

---

3. **(1 mark)** Define: Extraction ratio. State condition for high-clearance vs low-clearance drugs.

   **Solution:**
   - $E = CL_H/Q_H$
   - High $E$ (> 0.7): flow-limited clearance; oral bioavailability very low; sensitive to hepatic blood flow changes
   - Low $E$ (< 0.3): capacity-limited; sensitive to protein binding and intrinsic clearance changes

---

## Section B — Dissolution & Drug Release (High-Frequency)

4. **(2 marks)** Drug release data fit: $Q = 4.5\sqrt{t}$. Identify the model and calculate Q at t = 16 h.

   **Solution:**
   - Model: **Higuchi** (Q vs √t is linear → matrix diffusion)
   - $Q = 4.5\sqrt{16} = 4.5 \times 4 = 18$ mg/cm²

---

5. **(1 mark)** For a Korsmeyer-Peppas plot, the slope $n = 0.5$ for a slab system. Classify the mechanism.

   **Solution:** **Fickian diffusion** (n = 0.5 for slab geometry)

---

## Section C — Physical Pharmacy (Medium-Frequency)

6. **(2 marks)** A suspension has bulk density 0.45 g/mL and tapped density 0.65 g/mL. Calculate Carr's Index and Hausner Ratio. Comment on flow.

   **Solution:**
   - $CI = (0.65 - 0.45)/0.65 \times 100 = 30.8\%$ → Very poor flow
   - $HR = 0.65/0.45 = 1.44$ → Fair flow
   
   *Note: Both indices should agree; discrepancy suggests re-check measurement.*

---

7. **(1 mark)** At what pH is a weak acid (pKa 4.2) 50% ionised?

   **Solution:** pH = pKa = **4.2** (equal concentrations of HA and A⁻)

---

## Section D — Stability (Medium-Frequency)

8. **(2 marks)** A drug degrades by first-order kinetics with $k = 0.02$ month⁻¹. Calculate shelf life ($t_{90}$) and $t_{1/2}$.

   **Solution:**
   - $t_{1/2} = 0.693/0.02 = 34.7$ months
   - $t_{90} = 0.105/k = 0.105/0.02 = 5.25$ months

   *$t_{90}$ formula: $\ln(0.9) = -0.105$, so $t_{90} = 0.105/k$ for first-order*

---

9. **(1 mark)** Activation energy = 50 kJ/mol. Rate constant at 25°C = 0.001 s⁻¹. Estimate rate constant at 35°C.

   **Solution:**

   $$\ln\frac{k_2}{k_1} = \frac{E_a}{R}\left(\frac{1}{T_1}-\frac{1}{T_2}\right) = \frac{50000}{8.314}\left(\frac{1}{298}-\frac{1}{308}\right)$$

   $$= 6013 \times 1.087 \times 10^{-4} = 0.654$$

   $$k_2 = 0.001 \times e^{0.654} = 0.001 \times 1.92 = 0.00192 \text{ s}^{-1}$$

---

\newpage

# Quick Reference — Equation Flash Cards

> Cut out or cover the right column and test yourself before exam day.

| Formula | What it gives you |
|---------|-----------------|
| $0.693/k_e$ | Half-life |
| $C_0/k_e$ | AUC (IV bolus, 1-cpt) |
| $k_e \times V_d$ | Total clearance |
| $\ln(k_a/k_e)/(k_a-k_e)$ | T~max~ (oral) |
| $1/(1-e^{-k_e\tau})$ | Accumulation factor |
| $\sqrt{2DAC_s t}$ | Higuchi Q (matrix) |
| $k_H\sqrt{t}$ | Higuchi shorthand |
| $K_p \cdot C_{donor}$ | Transdermal flux |
| $D \cdot K/h$ | Permeability coefficient |
| $k_B T/(6\pi\eta r)$ | Diffusion coefficient (Stokes-Einstein) |
| $2r^2\Delta\rho g/9\eta$ | Stokes settling velocity |
| $20 \times M_H/M$ | HLB (Griffin) |
| $(ρ_t-ρ_b)/ρ_t \times 100$ | Carr's Index |
| $ρ_t/ρ_b$ | Hausner Ratio |
| $Ae^{-Ea/RT}$ | Rate constant (Arrhenius) |
| $C_0 - kt$ | Zero-order concentration |
| $C_0 e^{-kt}$ | First-order concentration |
| $i \cdot K_f \cdot m$ | Freezing point depression |
| pH = pKa | 50% ionisation point |
| $1-CL_H/Q_H$ | Oral bioavailability (first-pass) |

\newpage

---

# Appendix — Key Constants & Normal Values

| Constant/Parameter | Value |
|-------------------|-------|
| Gas constant R | 8.314 J/(mol·K) |
| Gas constant R | 0.082 L·atm/(mol·K) |
| Boltzmann constant $k_B$ | 1.38 × 10⁻²³ J/K |
| Avogadro's number | 6.022 × 10²³ mol⁻¹ |
| Hepatic blood flow $Q_H$ | ~1500 mL/min (1.5 L/min) |
| Renal blood flow | ~1200 mL/min |
| GFR | ~125 mL/min |
| Body weight (standard) | 70 kg |
| Total body water | ~42 L (0.6 L/kg) |
| Plasma volume | ~3 L |
| Blood volume | ~5 L |
| $K_f$ (water) | 1.86 °C·kg/mol |
| $K_b$ (water) | 0.512 °C·kg/mol |
| Normal blood serum ΔTf | −0.52 °C |
| Normal plasma pH | 7.35–7.45 |
| Normal body temperature | 37°C = 310 K |

---

> **Exam Strategy Reminder**
>
> - For any PK calculation: identify the model first (1-cpt vs 2-cpt, IV vs oral)
> - For dissolution: identify the plot type (linear → zero-order, ln → first-order, vs √t → Higuchi)
> - For stability: $t_{90} = 0.105/k$ for first-order; draw Arrhenius line for activation energy
> - For colloidal systems: |ζ| > 30 mV = stable; HLB determines emulsion type
> - When in doubt: write the full equation, substitute units, cancel dimensions

---

*End of GPAT Pharmaceutics Equation Bible — B.Pharm Sem 4–6*

*© GPAT Revision Series — For educational use only*
