# GRAVEPlot Step Plug-in v0.1.5

One change, and it matters beyond this plug-in.

## isAssociated now answers false

`GravePlotPlugin.isAssociated` returned true for every model. This plug-in
persists nothing in a model - its `load` and `save` are empty, and its state
lives in job streams - but answering true made every save write an empty
16-byte Plugin section for it into the MED file.

Feature plug-ins register alphabetically, so that section landed ahead of
MDOCS, the section holding the analyst's notes. CAFEAN's MED loader
dispatches only the first Plugin section in a file (`MEDUtil.loadModel` stops
scanning at the first Plugin block; `componentReadComplete` loads only what
was recorded), so from the first resave after v0.1.x was installed, model
notes stopped loading. On the save after that, `MDocsPlugin.isAssociated` saw
a model with no documents and the notes were dropped from the file entirely.

The sequence, on any model with notes:

1. save with v0.1.x installed - notes still in the file, GRAVEPlot section
   ahead of them
2. open - notes silently fail to load
3. save - notes gone from the file

Verified against SNAP 5.0.0 / TRACE 4.7.1 with the shipped
`Samples/TRACE/TypPWR/TraceTypPWR.med` (three notes: Title Page,
Introduction, Pipe 108).

Models at step 2 still hold their notes and load them again once this
version is installed. Models that reached step 3 have lost them.

The loader defect - only the first Plugin section of a file is ever
dispatched, and a section that fails to load is deleted by the next save -
is CAFEAN's, and is being reported to ISL separately. This release removes
this plug-in's contribution: a plug-in with nothing to persist must not
claim the model.
