# GRAVEPlot Step Plugin v0.1.3

- **Fixes the "0 Sources Expected" validation failure** (step drawn red)
  that appeared as soon as anything was connected to the data input: the
  data input is a file set, and the Model Editor derives its expected
  runtime files from a set-file-name list the step must maintain. The step
  now rebuilds that list from the live connections (in buildFileNames and
  updateStep), so expected always matches actual.
- **Connected data files stage as `data01`, `data02`, ...** in connection
  order — batch scripts open them by those names. Verified that GRAVEPlot's
  file importers accept the extensionless names (the file type is passed
  explicitly). The default script template documents this and its examples
  now reference `data01`.

As with v0.1.2: steps created under an older version keep their persisted
input slots — delete and re-add the step after upgrading.
