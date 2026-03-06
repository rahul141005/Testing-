#!/usr/bin/env bash
# =============================================================================
# generate-pdf.sh — Build the GPAT Pharmaceutics Equation Bible PDF
#
# Usage:
#   ./docs/generate-pdf.sh              # PDF via LaTeX (best quality)
#   ./docs/generate-pdf.sh --html       # HTML preview (no LaTeX required)
#   ./docs/generate-pdf.sh --wk         # PDF via wkhtmltopdf (no LaTeX)
#
# Requirements:
#   PDF  (default) : pandoc + a LaTeX distribution (TeX Live / MiKTeX)
#   HTML           : pandoc only
#   wkhtmltopdf    : pandoc + wkhtmltopdf
# =============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SRC="$SCRIPT_DIR/GPAT-Pharmaceutics-Equation-Bible.md"
CSS="$SCRIPT_DIR/gpat-style.css"
OUTPUT_DIR="$SCRIPT_DIR"
BASENAME="GPAT-Pharmaceutics-Equation-Bible"

# ── Dependency checks ──────────────────────────────────────────────────────

check_pandoc() {
  if ! command -v pandoc &>/dev/null; then
    echo "❌  pandoc not found. Install from https://pandoc.org/installing.html"
    exit 1
  fi
  echo "✔  pandoc $(pandoc --version | head -1 | awk '{print $2}')"
}

check_latex() {
  if ! command -v pdflatex &>/dev/null && ! command -v xelatex &>/dev/null && ! command -v lualatex &>/dev/null; then
    echo "❌  No LaTeX engine found (pdflatex / xelatex / lualatex)."
    echo "    Install TeX Live: https://www.tug.org/texlive/"
    echo "    Or use --html or --wk flag to skip LaTeX."
    exit 1
  fi

  if command -v xelatex &>/dev/null; then
    PDF_ENGINE="xelatex"
  elif command -v lualatex &>/dev/null; then
    PDF_ENGINE="lualatex"
  else
    PDF_ENGINE="pdflatex"
  fi
  echo "✔  LaTeX engine: $PDF_ENGINE"
}

# ── Build targets ──────────────────────────────────────────────────────────

build_pdf_latex() {
  check_pandoc
  check_latex

  OUT="$OUTPUT_DIR/$BASENAME.pdf"
  echo "📄  Building PDF (LaTeX) → $OUT"

  pandoc "$SRC" \
    --from=markdown+smart \
    --to=pdf \
    --pdf-engine="$PDF_ENGINE" \
    --toc \
    --toc-depth=3 \
    --number-sections \
    --highlight-style=tango \
    -V geometry:margin=2.5cm \
    -V fontsize=11pt \
    -V linestretch=1.4 \
    -V colorlinks=true \
    -V linkcolor=NavyBlue \
    -V urlcolor=NavyBlue \
    -V header-includes='\usepackage{amsmath}\usepackage{amssymb}\usepackage{booktabs}\usepackage{fancyhdr}\pagestyle{fancy}\fancyhead[L]{\small GPAT Pharmaceutics Equation Bible}\fancyhead[R]{\small B.Pharm Sem 4–6}\fancyfoot[C]{\thepage}' \
    --output="$OUT"

  echo "✅  PDF generated: $OUT"
}

build_html() {
  check_pandoc

  OUT="$OUTPUT_DIR/$BASENAME.html"
  echo "🌐  Building HTML → $OUT"

  pandoc "$SRC" \
    --from=markdown+smart \
    --to=html5 \
    --standalone \
    --toc \
    --toc-depth=3 \
    --number-sections \
    --mathjax \
    --highlight-style=tango \
    --css="gpat-style.css" \
    --metadata title="GPAT Pharmaceutics Equation Bible" \
    --output="$OUT"

  echo "✅  HTML generated: $OUT"
}

build_pdf_wkhtmltopdf() {
  check_pandoc

  if ! command -v wkhtmltopdf &>/dev/null; then
    echo "❌  wkhtmltopdf not found. Install from https://wkhtmltopdf.org/"
    exit 1
  fi
  echo "✔  wkhtmltopdf $(wkhtmltopdf --version 2>&1 | head -1)"

  HTML_TMP="$(mktemp -t gpat_XXXXXX).html"
  OUT="$OUTPUT_DIR/$BASENAME.pdf"

  echo "📄  Building PDF (wkhtmltopdf) → $OUT"

  pandoc "$SRC" \
    --from=markdown+smart \
    --to=html5 \
    --standalone \
    --toc \
    --toc-depth=3 \
    --number-sections \
    --mathjax \
    --css="$CSS" \
    --output="$HTML_TMP"

  wkhtmltopdf \
    --enable-local-file-access \
    --page-size A4 \
    --margin-top 20mm --margin-bottom 20mm \
    --margin-left 25mm --margin-right 25mm \
    --footer-center "[page]" \
    "$HTML_TMP" "$OUT"

  rm -f "$HTML_TMP"
  echo "✅  PDF generated: $OUT"
}

# ── Main ───────────────────────────────────────────────────────────────────

case "${1:-}" in
  --html)
    build_html
    ;;
  --wk | --wkhtmltopdf)
    build_pdf_wkhtmltopdf
    ;;
  *)
    build_pdf_latex
    ;;
esac
