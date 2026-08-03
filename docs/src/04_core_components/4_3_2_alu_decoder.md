<style>
.table-wrapper {overflow-x: visible}
</style>

## Purpose
The ALU Decoder lives inside the Control Unit, where it maps `ALUOp` values from the Main Decoder and `Funct` values from
instruction encodings to `ALUControl` values required by any given instruction.

`ALUOp` codes are used by non R-Type instructions to specify a subset of all `ALUControl` values.
This allows for other types of instructions to utilize portions of the ALU.

## Output Documentation

Below is a description of what each output of the ALU Decoder does.

### ALUControl

Relevant Pipeline Stages: `(3) Execute`
Bit Width: `4`<br>
Category: `Operation Code`

The ALU operation mapping for each value is given by the following table:

|    #    | ALU Operation               |
|:-------:|:----------------------------|
| `00000` | `Q = A & B`                 |
| `00001` | `Q = A \| B`                |
| `00010` | `Q = A ^ B`                 |
| `00011` | `Q = A << B` *(Logical)*    |
| `00100` | `Q = B >> A` *(Logical)*    |
| `00101` | `Q = A << B` *(Arithmetic)* |
| `00110` | `Q = A + B`                 |
| `00111` | `Q = A + B` *(Unsigned)*    |
| `01000` | `Q = A - B`                 |
| `01001` | `Q = A - B` *(Unsigned)*    |
| `01010` | `Q = A * B`                 |
| `01011` | `Q = A * B` *(Unsigned)*    |
| `01100` | `Q = A / B`                 |
| `01101` | `Q = A / B` *(Unsigned)*    |
| `01110` | `slt Q, A, B`               |
| `01111` | `sltu Q, A, B`              |
| `10000` | `mfhi`                      |
| `10001` | `mthi`                      |
| `10010` | `mflo`                      |
| `10011` | `mtlo`                      |
| `101xx` | `N/A`                       |
| `11xxx` | `N/A`                       |

> [!TIP]
> The ALU Operations in this table are formatted where `Q` is the output, `A` is the first input, and `B`
> is the second input (if any).
