# LogiSpim Application FAQ
Click on any question below to view an answer.

<details>
    <summary><h3 style="display: inline; margin: 0">Q: How do I compile a MIPS program?</h3></summary>

First, ensure LogiSpim is installed as per the [installation guide](../../03_installation/index.md)
and your MIPS program fits [this criteria](../../04_usage_guide/index.md#mips-program).

Then, see [this section](../../04_usage_guide/index.md#logispim-application) of the usage guide.

To load the compiled files into the processor, see [the following](../../04_usage_guide/index.md#logispim-processor)
section of the usage guide.
___
</details><br>

<details>
    <summary><h3 style="display: inline; margin: 0">Q: Why does the <code>Open Logisim</code> button not work?</h3></summary>

This is likely due to one of the following:
 - Logisim-evolution v5.0.0+ is not installed as an application on your device
   - Ensure you followed the steps for installing Logisim-evolution outlined in the [installation guide](../../03_installation/index.md).
 - Logisim-evolution v5.0.0+ is not the application associated with `.circ` files
   - Open your workspace directory and attempt to open the `LogiSpim-<version>.circ` file through some
   form of `Open with` menu where you can select Logisim-evolution, and set it as the default program for `.circ` files.
___
</details><br>

