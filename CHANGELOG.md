# Changelog

## 1.0.3

- Lowered minimum supported IntelliJ Platform build to `261` for IntelliJ IDEA 2026.1 compatibility metadata.
- Rebuilt plugin package for JetBrains Marketplace verification.

## 1.0.2

- Removed unused glyph-kind rendering code left from earlier minimap rendering experiments.
- Removed inactive VCS change placeholder UI and collector to reduce runtime footprint.
- Updated plugin icons with the selected CL + minimap design.
- Rebuilt plugin package for JetBrains Marketplace submission.

## 1.0.1

- Replaced deprecated `Document.addDocumentListener(DocumentListener)` usage with disposable-bound listener registration.
- Replaced deprecated no-argument `RangeHighlighter.getTextAttributes()` usage with color-scheme-aware API.
- Rebuilt plugin package for JetBrains Marketplace verification.

## 1.0.0

- Initial public release for local testing and GitHub preparation.
- Added editor minimap for IntelliJ IDEA.
- Added compact code texture rendering.
- Added BufferedImage-based raster rendering.
- Added unified minimap layout to avoid stretching short files.
- Added fold-aware line mapping.
- Added viewport and caret line indicators.
- Added error, warning, and markup indicators.
- Added minimap width resizing.
- Added optional default vertical scrollbar hiding.
- Added large-file performance safeguards.
- Added settings page under `Settings | Tools | CodeLens Pro`.
- Added toggle action with `Ctrl + Shift + G`.

## Acknowledgements

CodeLens Pro was inspired by CodeGlance Pro:

- GitHub: https://github.com/Nasller/CodeGlancePro
- JetBrains Marketplace: https://plugins.jetbrains.com/plugin/18824-codeglance-pro

CodeLens Pro is not affiliated with, endorsed by, or maintained by the CodeGlance Pro project or its authors.
