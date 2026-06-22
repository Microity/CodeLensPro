# Changelog

## 1.0.5

- Improved minimap placement across the IDE.
- Removed the minimap from small dialog input fields while keeping editor, diff, terminal, console, and run log support.

## 1.0.4.1

- Updated plugin description, acknowledgements, vendor, and privacy information.

## 1.0.4

- Refined Marketplace description and release metadata.

## 1.0.3

- Updated compatibility metadata for IntelliJ Platform 2026.1+.

## 1.0.2

- Reduced package size by removing unused code and placeholder features.
- Updated plugin icons.

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
