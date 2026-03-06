# Testing-

## GPAT Pharmaceutics Equation Bible

**Complete Formula Handbook — B.Pharm Sem 4–6**

This repository contains a professionally structured, PDF-ready GPAT revision handbook covering all key pharmaceutics equations for B.Pharm Semesters 4–6.

### Contents

| File | Description |
|------|-------------|
| [`docs/GPAT-Pharmaceutics-Equation-Bible.md`](docs/GPAT-Pharmaceutics-Equation-Bible.md) | Handbook source (Markdown) |
| [`docs/generate-pdf.sh`](docs/generate-pdf.sh) | PDF generation script |
| [`docs/gpat-style.css`](docs/gpat-style.css) | CSS stylesheet for HTML/PDF output |

### Topics Covered

1. **Biopharmaceutics & Pharmacokinetics** — half-life, Vd, CL, AUC, bioavailability, accumulation, renal/hepatic clearance
2. **Dissolution & Drug Release** — Noyes-Whitney, Higuchi, Korsmeyer-Peppas, zero/first-order, Hixson-Crowell
3. **Diffusion & Membrane Transport** — Fick's laws, Henderson-Hasselbalch, partition coefficient
4. **Rheology** — viscosity, flow behaviour, Bingham plastic, thixotropy
5. **Surface Chemistry & Colloids** — HLB, CMC, Stokes' law, zeta potential, DLVO theory
6. **Thermodynamics & Stability** — Arrhenius, Q₁₀ concept, reaction orders, colligative properties
7. **Particle Science & Powder Technology** — Carr's Index, Hausner Ratio, angle of repose, BET
8. **Dosage Form Design** — tablets, suspensions, emulsions, buffer capacity, isotonicity
9. **Novel Drug Delivery Systems** — reservoir systems, OROS, nanoparticles, transdermal, liposomes
10. **GPAT Trap Questions & Practice Problems** — 10 common traps + solved exam questions

---

## Generating the PDF

### Option 1 — LaTeX (best quality, ~25–35 pages)

Requires [Pandoc](https://pandoc.org/installing.html) and a LaTeX distribution
([TeX Live](https://www.tug.org/texlive/) / [MiKTeX](https://miktex.org/)).

```bash
# From the repository root
./docs/generate-pdf.sh
# Output: docs/GPAT-Pharmaceutics-Equation-Bible.pdf
```

### Option 2 — HTML preview (Pandoc only)

```bash
./docs/generate-pdf.sh --html
# Output: docs/GPAT-Pharmaceutics-Equation-Bible.html
# Open in browser, then use browser Print → Save as PDF
```

### Option 3 — wkhtmltopdf (no LaTeX)

Requires Pandoc and [wkhtmltopdf](https://wkhtmltopdf.org/).

```bash
./docs/generate-pdf.sh --wk
# Output: docs/GPAT-Pharmaceutics-Equation-Bible.pdf
```

### Manual Pandoc command

```bash
pandoc docs/GPAT-Pharmaceutics-Equation-Bible.md \
  --from=markdown+smart \
  --to=pdf \
  --pdf-engine=xelatex \
  --toc --toc-depth=3 \
  --number-sections \
  -V geometry:margin=2.5cm \
  -V fontsize=11pt \
  --output=docs/GPAT-Pharmaceutics-Equation-Bible.pdf
```

---

## Quick Start (no tooling required)

The Markdown source renders correctly on GitHub — browse the handbook directly at
[`docs/GPAT-Pharmaceutics-Equation-Bible.md`](docs/GPAT-Pharmaceutics-Equation-Bible.md).

---

*For educational use — GPAT Revision Series*