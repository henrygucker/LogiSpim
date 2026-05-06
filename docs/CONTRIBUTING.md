# Getting Started

Documentation for LogiSpim uses [mdBook](https://github.com/rust-lang/mdBook).
This allows for the documentation to be easily written in [Markdown](https://www.markdownguide.org) while allowing for
more complex elements with direct html.
Additionally, support for [Mermaid.js](https://mermaid.js.org) graphs has been added through the
[mdbook-mermaid](https://github.com/badboy/mdbook-mermaid) plugin.

## Installing mdBook
To begin writing documentation, both [mdBook](https://github.com/rust-lang/mdBook) and 
[mdbook-mermaid](https://github.com/badboy/mdbook-mermaid) must be installed.

This can be done by following details on [mdBook's installation guide](https://rust-lang.github.io/mdBook/guide/installation.html)
or can be done directly if you have Rust and Cargo installed from the [Rust installation guide](https://rust-lang.org/tools/install/).

To install mdBook with Rust and Cargo, it is as simple as running:
```shell
cargo install mdbook
```

Once mdBook is installed, it is possible to install plugins.
These plugins expand the capability of mdBook, allowing for more flexibility when writing documentation.

### Installing Mermaid
Installation guidelines are listed in the [README.md file of the mdbook-mermaid respository](https://github.com/badboy/mdbook-mermaid/blob/main/README.md).
If you have Rust and Cargo installed, mdbook-mermaid can be installed easily through the following command:
```shell
cargo install mdbook-mermaid
```

## Previewing Documentation
With mdBook and any plugins, it is easy to preview any changes to documentation in real-time.
After entering the `docs` directory in your terminal, run the following command to open a live updating mdBook
page that responds instantly to updates to its source code:
```shell
mdbook serve --open
```

# Guidelines
Since LogiSpim is designed as an educational tool, accessibility to the material should be emphasized.
The documentation is set up in a way such that minimal outside knowledge is required to approach the topic.
Simultaneously, the documentation must also explain the function and purpose behind components and circuitry in the
Logisim Circuit.

## File Structure
As is displayed in the book's `SUUMMARY.md` file, the documentation is broken up into chapters.
Each chapter has a corresponding directory.
Inside each of those directories are the files that make up the subchapters. 
The subchapters are named and numbered based on their position in the resulting documentation.
This structure is relatively straight forward and allows for easy expansion of the documentation.

Note that the core file of each directory/chapter is named `index.md`.
This allows for easily linking between mdBook files/pages.
With this naming scheme, it is possible to reference to another page as follows:
```markdown
see [this other page](relative/path//to/other/page).
```

## Images/Assets
When including images in documentation, place assets in an `assets` directory in the same directory as the documentation's
source file.
This allows for images to be included easily in documentation pages through the following form:
```markdown
![Replacement Text](assets/image-name.png)
```

# Tests
When making changes to components such as the Main Decoder, it is very easy to make a mistake and cause the component to
perform incorrectly.
To prevent this, tests were created to ensure that all references to its outputs are consistent.
When modifying the documentation page on the main decoder, it is critical that the test is run to ensure no mismatching occurs.
This is done as a JUnit test through Maven.

## Process for Modifying the Main Decoder
When any changes to the desired output of the Main Decoder are made, those changes must be reflected in
`Testing/src/test/java/MainDecoderMaster.java` and the tables displaying the Main Decoder's desired outputs in the
documentation must match.
To update the table in the documentation, it is possible to run the `MainDecoderMaster.java` file's `main(...)` method
to have updated versions of the markdown tables be printed.
These tables are to be updated in the correct documentation file before a pull request is submitted.




