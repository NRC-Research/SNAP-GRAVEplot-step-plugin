# GRAVEPlot Step Plugin v0.1.1

Corrects the generated `graveplot` application locations to match the actual
RESGC image installs (RESGC-packer `install_grave` roles):

- Linux/mac default: `/opt/grave/bin/graveplot.sh` (was `/opt/graveplot/...`)
- Windows default: `C:\GRAVE\bin\graveplot.bat`
  (was `C:\Program Files\graveplot\...`)

No step, wrapper, or persistence changes — streams saved with v0.1.0 are
unaffected. Existing user-configured application locations are never
overwritten, so hosts that already point at a local GRAVEPlot keep working.
