# Pdf Metadata Editor

An advanced GUI editor for PDF Metadata. It is a Java-based graphical user interface (GUI) application that allows users to edit metadata in PDF files. 
The application utilizes [Apache PDFBox](https://pdfbox.apache.org/) as its core library for PDF processing and metadata manipulation.

## Installation

### Prebuild installers

Prebuilt installers for all supported operating systems  can be found from the [official website](https://pdf.metadata.care/) 
or from the releases section in this repository.

### Flathub

```sh
flatpak install flathub me.broken_by.PdfMetadataEditor
```
    

### Arch Linux (AUR)

Arch Linux users can install the package from the AUR.
```sh
yay -S pdf-metadata-editor-bin
```

## Building from source

### Build JAR 

```sh
mvn clean package -DskipTests -Dpackages.skip=true
```

You will find the app JAR in `modules/editor-gui/target/jar/`

### Build OS specific installer/app

```sh
mvn clean package -DskipTests
```

This will build all supported installers for your current OS and 
place them in `modules/editor-gui/target/packages/`

## Help

Documentation is available at the [Help page](https://pdf.metadata.care/help/) 

## License

Distributed under the MIT License. See the [LICENSE](LICENSE) file for more information.