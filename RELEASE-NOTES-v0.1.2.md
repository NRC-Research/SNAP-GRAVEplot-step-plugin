# GRAVEPlot Step Plugin v0.1.2

- The step's **data input set now accepts `TRACE:XTV`** in addition to
  `Generic:Ascii` and `Generic:TH`, so TRACE plot files (xtv/dmx) connect
  directly. GRAVEPlot 1.0.x reads them natively through its analysis-code
  bindings (code type `TRACE`, file type `XTV`).
- The **default script template gains a TRACE XTV example** using
  `build_dataset_from_file(path, "TRACE", "XTV", use_si, channel)`,
  verified end-to-end against GRAVEPlot 1.0.1-nrc1 (SNAP's sample
  `trace.xtv`, channel `pn-1A01`, rendered PNG).

Note: input slots persist per step in the `.med` — steps created with
v0.1.1 keep the old data-input types. Delete and re-add the step after
upgrading to get the XTV-accepting input.
