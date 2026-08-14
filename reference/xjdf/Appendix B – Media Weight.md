# Appendix B

## B Media Weight

In North America and Japan, each grade of paper has one basic size used to compute its basis weight per ream. For example, Bond basic size is 17" × 22" and Shiroku-ban basic size is 788 mm × 1091 mm.

### B.1 North American Media Weight

In North America, a paper's basis weight is the weight of five hundred sheets of its basic size. For example, if five hundred 25" × 38" sheets of offset paper weigh 60 pounds, it is called 60# offset. Paper mills outside of North America use the metric system to designate paper weight. The basis weight of foreign papers is grams per square meter (g/m²) known as the sheet's grammage. Papers made to metric standards don't convert to basis weights familiar to North Americans.

For example, 100 g/m² equals a basis weight of 67.5lb. Following is the English/grammage conversion formula:
`Basis Weight(lb.) × (1406.5 / Square inches in basic size) = grams per square meter`

For example, the grammage of 65 lb. cover stock when the cover is 20 × 26 can be calculated as follows:
`65 × (1406.5 / (20 × 26)) = 65 × 2.70 = 176 g/m²`

The following table defines the basic sizes and the factor that the North American weight is multiplied by to calculate `@Weight` for various stock types. Stock type is specified in `Media/@StockType` or `MediaIntent/@StockType`.

**Table B.1: Conversion Factor from Basis Weight(lbs) to Weight(g/m²)**

| STOCK TYPE | BASIS SIZE IN INCHES | CONVERSION FACTOR | EQUIVALENT |
| --- | --- | --- | --- |
| Bond | 17 × 22 | 3.76 | "Ledger", "Manifold" |
| Book | 25 × 38 | 1.48 | "Bible", "Coated", "Offset", "Text" |
| Bristol | 22½ × 28½ | 2.19 | |
| Cover | 20 × 26 | 2.70 | |
| Index | 25½ × 30½ | 1.81 | |
| Newsprint | 24 × 36 | 1.63 | "Tag" |

**Table B.2: Grammage Equivalents for Common(US) Basis Weights**

| GRAMMAGE (G/M²) | BASIS WEIGHT | GRAMMAGE (G/M²) | BASIS WEIGHT |
| --- | --- | --- | --- |
| 30 | 20# Book | 150 | 40# Ledger |
| 34 | 9# Manifold | 152 | 60# Cover |
| 36 | 24# Book | 163 | 90# Index |
| 44 | 30# Book | 163 | 100# Tag |
| 45 | 12# Manifold | 175 | 80# Bristol |
| 49 | 13# Bond | 176 | 65# Cover |
| 49 | 33# Book | 178 | 120# Book |
| 52 | 35# Book | 197 | 90# Bristol |
| 59 | 40# Book | 199 | 110# Index |
| 60 | 16# Bond | 204 | 125# Tag |
| 67 | 45# Bond | 216 | 80# Cover |
| 74 | 50# Book | 219 | 100# Bristol |
| 75 | 20# Bond | 244 | 150# Tag |
| 81 | 55# Book | 253 | 140# Index |
| 89 | 60# Book | 263 | 120# Bristol |
| 90 | 24# Bond | 270 | 100# Cover |
| 104 | 70# Book | 285 | 175# Tag |
| 105 | 28# Ledger | 307 | 140# Bristol |
| 108 | 40# Cover | 307 | 170# Index |
| 118 | 80# Book | 325 | 200# Tag |
| 120 | 32# Ledger | 350 | 160# Bristol |
| 133 | 90# Book | 352 | 130# Cover |
| 135 | 36# Ledger | 394 | 180# Bristol |
| 135 | 50# Cover | 398 | 220# Index |
| 147 | 67# Bristol | 407 | 250# Tag |
| 148 | 100# Book | 438 | 200# Bristol |
| | | 488 | 300# Tag |

### B.2 Japanese Media Weight

In Japan, a paper's basis weight is the weight of 1000 sheets of its basic size and ream weights are given in kg.
Following is the Japanese/grammage conversion formula:
`Basis Weight(kg) / Basic Size(m²) = grams per square meter`

For example, the grammage of 70 kg Shiroku-ban stock when the size is 0.788 × 1.091 can be calculated as follows:
`70 / (0.788 × 1.091) = 81.4 g/m²`

**Table B.3: Japanese Media Weight**

| STOCK TYPE | SHIROKU-BAN 788 X 1091 | JIS B-BAN 765 X 1085 | KIKU-BAN 636 X 939 | JIS A-BAN 625 X 880 | GRAMMAGE (G/M²) |
| --- | --- | --- | --- | --- | --- |
| Aatoposutoshi アートポスト紙 | 180 | - | 125 | - | 209.3 |
| Aatoposutoshi アートポスト紙 | 200 | - | 139 | - | 232.6 |
| Aatoposutoshi アートポスト紙 | 220 | - | 153 | - | 255.0 |
| Aatoshi アート紙 | 73 | 70.5 | 50.5 | 46.5 | 84.9 |
| Aatoshi アート紙 | 90 | 87 | 62.5 | 57.5 | 104.7 |
| Aatoshi アート紙 | 110 | 106 | 76.5 | 70.5 | 127.9 |
| Aatoshi アート紙 | 135 | 130.5 | 93.5 | 86.5 | 157.0 |
| Chuushitsushi 中質紙 | - | 45 | - | 30 | 54.2 |
| Chuushitsushi 中質紙 | - | 55 | - | 36.5 | 66.3 |
| Joushitsushi 上質紙 | 40 | - | - | - | 46.5 |
| Joushitsushi 上質紙 | 45 | - | 31 | 20.5 | 52.3 |
| Joushitsushi 上質紙 | 55 | 53 | 38 | 35 | 64.0 |
| Joushitsushi 上質紙 | 70 | 67.5 | 48.5 | 44.5 | 81.4 |
| Joushitsushi 上質紙 | 90 | - | 62.5 | 47.5 | 104.7 |
| Joushitsushi 上質紙 | 110 | - | 71.5 | 70.5 | 127.9 |
| Joushitsushi 上質紙 | 135 | - | 93.5 | 80.5 | 157.0 |
| Joushitsushi 上質紙 | 180 | - | - | - | 209.3 |
| Mashinkootoshi マシンコート紙 | 63 | 61 | - | - | 73.3 |
| Mashinkootoshi マシンコート紙 | 68 | 65.6 | 47 | 43.5 | 79.1 |
| Mashinkootoshi マシンコート紙 | 73 | 70.5 | 50.5 | 46.5 | 84.9 |
| Mashinkootoshi マシンコート紙 | 90 | 87 | 62.5 | 57.5 | 104.7 |
| Mashinkootoshi マシンコート紙 | 110 | 106 | 76.5 | 70.5 | 127.9 |
| Mashinkootoshi マシンコート紙 | 135 | 130.5 | 93.5 | 86.5 | 157.0 |

The following describes the five stock types in the above table:
*   **上質紙 Joushitsushi** (“top-quality paper”) contains 100% chemical pulp
*   **中質紙 Chuushitsushi** (“medium-quality paper”) contains a minimum of 70% chemical pulp
*   **アート紙 Aatoshi** (“art paper”) is machine coated paper, available in top quality and medium quality (Joushitsu and Chuushitsu)
*   **マシンコート紙 Mashinkootoshi** (“machine coated paper”), also called Kootoshi (コート紙), is machine coated paper given only a thin coat of clay
*   **アートポスト紙 Aatoposutoshi** (“art-post paper”) is cover stock coated on one side

### B.3 Paper Grade

[ISO12647-2:2004] provides a rough classification of offset paper with 5 classes, which is generally referred to as paper grade. [ISO12647-2:2004] was updated in 2013, and a new set of 8 standard papers was defined that are more appropriate for offset paper types that are used today. [ISO12647-3:2013] defines the grade for news paper printing.
[ISO12647-4:2014] defines the properties of rotogravure papers. [ISO12647-2:2023] is specifically for the Japanese and packaging markets.

The following tables provide a rough and non-normative translation between the newer (i.e., post 2004) classifications and the earlier 2004 classifications and press grades.
*Note: The column ‘ID’ refers to values of the ISOPaperSubstrate enumeration.*

#### B.3.1 Translation between ISO12647-2:2013 and ISO12647-2:2004

**Table B.4: Translation of Paper grades between ISO12647-2:2013 and ISO12647-2:2004**

| ISO12647-2:2013 ID | ISO12647-2:2013 TYPE | PRESS | ISO12647-2:2004 GRADE | ISO12647-2:2004 TYPE |
| --- | --- | --- | --- | --- |
| PS1 | Premium coated, moderate fluorescence. | Offset sheet and web. | 1/2 | Gloss and matte coated, sheet and web. |
| PS2 | Improved coated, low fluorescence. | Offset web. | 3 | Gloss coated, web. |
| PS3 | Standard glossy coated, low fluorescence. | Offset web. | 3 | Gloss coated, web. |
| PS4 | Standard matte coated, low fluorescence. | Offset web. | 2 | Matte coated, web. |
| PS5 | Wood free uncoated, high fluorescence. | Offset sheet and web. | 4 | Uncoated white, sheet and web. |
| PS6 | Super calendered, low fluorescence. | Offset web. | 4 | Uncoated white, web. |
| PS7 | Improved uncoated, faint fluorescence. | Offset web. | 4 | Uncoated white, web. |
| PS8 | Standard uncoated, faint fluorescence. | Offset web. | 4/(5) | Uncoated white, web (but not as yellowish as in grade 5). |

#### B.3.2 Translation between ISO12647-2:2023 and ISO12647-2:2004

**Table B.5: Translation of Paper grades between ISO12647-2:2023 and ISO12647-2:2004**

| ISO12647-2:2023 ID | ISO12647-2:2023 TYPE | PRESS | ISO12647-2:2004 GRADE | ISO12647-2:2004 TYPE |
| --- | --- | --- | --- | --- |
| PS9 | Premium coated, faint fluorescence (unmeasured). | Offset sheet and web. | 1/2 | Gloss and matte coated, sheet and web (and Japan color 2001 coated). |

#### B.3.3 Translation between ISO12647-3:2013 and ISO12647-2:2004

**Table B.6: Translation of Paper grades between ISO12647-3:2013 and ISO12647-2:2004**

| ISO12647-3:2013 ID | ISO12647-3:2013 TYPE | PRESS | ISO12647-2:2004 GRADE | ISO12647-2:2004 TYPE |
| --- | --- | --- | --- | --- |
| SNP | Standard newsprint. | Offset newsprint | 5 | Uncoated. |

#### B.3.4 Translation between ISO12647-4:2014 and ISO12647-2:2004

**Table B.7: Translation of Paper grades between ISO12647-4:2014 and ISO12647-2:2004**

| ISO12647-4:2014 ID | ISO12647-4:2014 TYPE | PRESS | ISO12647-2:2004 GRADE | ISO12647-2:2004 TYPE |
| --- | --- | --- | --- | --- |
| LWCPlus | Light weight calendered plus. | Rotogravure | 3 | Gloss coated, web. |
| LWCStandard | Light weight calendered standard. | Rotogravure | 3 | Gloss coated, web. |
| NewsPlus | Newsprint plus. | Rotogravure | 5 | Newsprint improved was not yet part of ISO12647-2 and is similar to uncoated, web, as well as SNP from ISO12647-3. |
| SCPlus | Super calendered plus. | Rotogravure | 4 | Uncoated white, web. |
| SCStandard | Super calendered standard. | Rotogravure | 4 | Uncoated white, web. |
