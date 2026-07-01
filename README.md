# CodeLens Pro

CodeLens Pro is a lightweight editor minimap plugin for IntelliJ IDEA. It adds a compact code overview next to the editor, helping you navigate files faster and understand code structure at a glance.

Repository: <https://github.com/Microity/CodeLensPro.git>

## Features

- Compact editor minimap.
- Code texture rendering with theme-aware colors.
- Fold-aware minimap layout.
- Short-file layout protection so small files are not stretched to fill the whole editor height.
- Viewport indicator.
- Caret line marker.
- Error, warning, and markup indicators.
- Resizable minimap width.
- Optional hiding of the default vertical scrollbar.
- Large-file safeguards to reduce memory usage and UI overhead.
- Toggle action with `Ctrl + Shift + G`.
- Settings page under `Settings | Tools | CodeLens Pro`.

## Usage

1. Install the plugin from disk or from JetBrains Marketplace after publication.
2. Open a file in IntelliJ IDEA.
3. The minimap appears beside the editor.
4. Use `Ctrl + Shift + G` to toggle CodeLens Pro.
5. Open `Settings | Tools | CodeLens Pro` to customize behavior.

You can also resize the minimap by dragging its left edge, and click or drag inside the minimap to navigate the editor.

## Performance

CodeLens Pro is designed to stay lightweight:

- It uses compact snapshot data structures.
- It renders the minimap code texture into a cached image.
- It throttles repaint operations.
- It debounces document rebuilds.
- It falls back to lighter rendering modes for large files.

## Privacy

CodeLens Pro works locally inside the IDE. It does not collect, transmit, or store source code, project files, telemetry, or personal data.

## Compatibility

CodeLens Pro targets IntelliJ IDEA 2026.1+ and IntelliJ Platform build `261+`.

When building from source, provide the local IntelliJ IDEA installation path with either the `localIdePath` Gradle property or the `CODELENS_PRO_IDE_PATH` environment variable.

Example:

```powershell
gradle clean test buildPlugin -PlocalIdePath="D:/Path/To/IntelliJ IDEA"
```

## Acknowledgements

CodeLens Pro was inspired by the excellent CodeGlance Pro plugin.

CodeGlance Pro:

- GitHub: <https://github.com/Nasller/CodeGlancePro>
- JetBrains Marketplace: <https://plugins.jetbrains.com/plugin/18824-codeglance-pro>

CodeLens Pro references the general minimap interaction and rendering ideas from CodeGlance Pro, while using its own plugin identity, package namespace, settings, implementation, and user experience.

CodeLens Pro is not affiliated with, endorsed by, or maintained by the CodeGlance Pro project or its authors.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
