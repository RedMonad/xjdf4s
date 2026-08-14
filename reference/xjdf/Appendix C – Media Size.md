# Appendix C

## C Media Size

The following table defines a set of named media sizes as defined by [PPD].

**Implementation Remark**
The following tables provide the dimensions for various ranges of named paper sizes. Each named size has dimensions listed in multiple columns for different units (points, millimeters and inches). One of these units is normative and is identified in the column header. The others are conversions from the normative size, shown for convenience. Since these sizes are real numbers, comparison of media dimensions SHOULD take into account certain rounding errors. Therefore, different media sizes SHOULD be considered equal when both dimensions are the same within a range of 5 points.

### C.1 Architectural Paper Sizes

**Table C.1: Architectural Paper Sizes**

| MEDIA SIZE | SIZE IN POINTS | SIZE IN MILLIMETERS | SIZE IN INCHES (NORMATIVE) |
| --- | --- | --- | --- |
| ArchA | 648 × 864 | 228.6 × 304.8 | 9 × 12 |
| ArchB | 864 × 1296 | 304.8 × 457.2 | 12 × 18 |
| ArchC | 1296 × 1728 | 457.2 × 609.6 | 18 × 24 |
| ArchD | 1728 × 2592 | 609.6 × 914.4 | 24 × 36 |
| ArchE | 2592 × 3456 | 914.4 × 1219.2 | 36 × 48 |
| ArchE1 | 2160 × 3024 | 762.0 × 1066.8 | 30 × 42 |
| ArchE2 | 1872 × 2736 | 660.4 × 965.2 | 26 × 38 |
| ArchE3 | 1944 × 2808 | 685.8 × 990.6 | 27 × 39 |

### C.2 Business Card Sizes

**Table C.2: Business Card Sizes**

| MEDIA SIZE | SIZE IN POINTS | SIZE IN MILLIMETERS (NORMATIVE) | SIZE IN INCHES |
| --- | --- | --- | --- |
| BusinessCard_Japan | 156 × 258 | 55 × 91 | 2.2 × 3.6 |
| BusinessCard_UK | 156 × 241 | 55 × 85 | 2.2 × 3.3 |
| BusinessCard_US | 145 × 252 | 51 × 89 | 2.0 × 3.5 |

### C.3 International A Paper Sizes

These sizes are defined by ISO standards, including [ISO216:2007] and by JIS standards [JIS P0138] except where noted.

**Table C.3: International A Paper Sizes**

| MEDIA SIZE | SIZE IN POINTS | SIZE IN MILLIMETERS (NORMATIVE) | SIZE IN INCHES |
| --- | --- | --- | --- |
| A0 | 2384 × 3370 | 841 × 1189 | 33.11 × 46.81 |
| A1 | 1684 × 2384 | 594 × 841 | 23.39 × 33.11 |
| A2 | 1191 × 1684 | 420 × 594 | 16.54 × 23.39 |
| A3 | 842 × 1191 | 297 × 420 | 11.69 × 16.54 |
| A3Extra *a* | 913 × 1262 | 322 × 445 | 12.67 × 17.52 |
| A4 | 595 × 842 | 210 × 297 | 8.27 × 11.69 |
| A4Extra *a* | 667 × 914 | 235 × 322 | 9.25 × 12.67 |
| A4Plus *a* | 595 × 936 | 210 × 330 | 8.27 × 13.00 |
| A4Tab *a* | 638 × 842 | 225 × 297 | 8.86 × 11.69 |
| A5 | 420 × 595 | 148 × 210 | 5.83 × 8.27 |
| A5Extra *a* | 492 × 668 | 174 × 235 | 6.85 × 9.25 |
| A6 | 297 × 420 | 105 × 148 | 4.13 × 5.83 |
| A7 | 210 × 297 | 74 × 105 | 2.91 × 4.13 |
| A8 | 148 × 210 | 52 × 74 | 2.05 × 2.91 |
| A9 | 105 × 148 | 37 × 52 | 1.46 × 2.05 |
| A10 | 73 × 105 | 26 × 37 | 1.02 × 1.46 |

*a. Non-standard ISO size variations.*

### C.4 International and Japanese B Paper Sizes

These sizes are defined by ISO standards, including [ISO216:2007] and by JIS standards [JIS P0138].
*Note: Equivalent International and Japanese B paper sizes, i.e. ISOB0/JISB0, ISOB1/JISB1 etc., differ in area and size. To illustrate this point the ISOB0 sheet has an area of 1m² whereas the JISB0 sheet has an area of 1.5m². The aspect ratio of both is identical. Implementations SHOULD NOT calculate values and SHOULD use the values from the respective tables below.*

#### C.4.1 International(ISO) B Paper Sizes

**Table C.4: International B Paper Sizes**

| MEDIA SIZE | SIZE IN POINTS | SIZE IN MILLIMETERS (NORMATIVE) | SIZE IN INCHES |
| --- | --- | --- | --- |
| ISOB0 | 2834 × 4008 | 1000 × 1414 | 39.4 × 55.7 |
| ISOB1 | 2004 × 2834 | 707 × 1000 | 27.8 × 39.4 |
| ISOB2 | 1417 × 2004 | 500 × 707 | 19.7 × 27.8 |
| ISOB3 | 1001 × 1417 | 353 × 500 | 13.9 × 19.7 |
| ISOB4 | 709 × 1001 | 250 × 353 | 9.8 × 13.9 |
| ISOB5 | 499 × 709 | 176 × 250 | 6.9 × 9.8 |
| ISOB6 | 354 × 499 | 125 × 176 | 4.9 × 6.9 |
| ISOB7 | 249 × 354 | 88 × 125 | 3.5 × 4.9 |
| ISOB8 | 176 × 249 | 62 × 88 | 2.4 × 3.5 |
| ISOB9 | 125 × 176 | 44 × 62 | 1.7 × 2.4 |
| ISOB10 | 88 × 125 | 31 × 44 | 1.2 × 1.7 |

#### C.4.2 Japanese(JIS) B Paper Sizes

**Table C.5: Japanese(JIS) B Paper Sizes**

| MEDIA SIZE | SIZE IN POINTS | SIZE IN MILLIMETERS (NORMATIVE) | SIZE IN INCHES |
| --- | --- | --- | --- |
| JISB0 | 2920 × 4127 | 1030 × 1456 | 40.55 × 57.32 |
| JISB1 | 2064 × 2920 | 728 × 1030 | 28.66 × 40.55 |
| JISB2 | 1460 × 2064 | 515 × 728 | 20.28 × 28.66 |
| JISB3 | 1032 × 1460 | 364 × 515 | 14.33 × 20.28 |
| JISB4 | 729 × 1032 | 257 × 364 | 10.12 × 14.33 |
| JISB5 | 516 × 729 | 182 × 257 | 7.17 × 10.12 |
| JISB6 | 363 × 516 | 128 × 182 | 5.04 × 7.17 |
| JISB7 | 258 × 363 | 91 × 128 | 3.58 × 5.04 |
| JISB8 | 181 × 258 | 64 × 91 | 2.52 × 3.58 |
| JISB9 | 127 × 181 | 45 × 64 | 1.77 × 2.52 |
| JISB10 | 91 × 127 | 32 × 45 | 1.26 × 1.77 |

### C.5 International C Envelope Sizes

These sizes are defined by ISO standards, including [ISO216:2007].

**Table C.6: International C Envelope Sizes**

| MEDIA SIZE | SIZE IN POINTS | SIZE IN MILLIMETERS (NORMATIVE) | SIZE IN INCHES |
| --- | --- | --- | --- |
| C0 | 2599 × 3676 | 917 × 1297 | 36.1 × 51.1 |
| C1 | 1837 × 2599 | 648 × 917 | 25.5 × 36.1 |
| C2 | 1298 × 1837 | 458 × 648 | 18.0 × 25.5 |
| C3 | 918 × 1298 | 324 × 458 | 12.8 × 18.0 |
| C4 | 649 × 918 | 229 × 324 | 9.0 × 12.8 |
| C5 | 459 × 649 | 162 × 229 | 6.4 × 9.0 |
| C6 | 323 × 459 | 114 × 162 | 4.5 × 6.4 |
| C7 | 230 × 323 | 81 × 114 | 3.2 × 4.5 |
| C8 | 162 × 230 | 57 × 81 | 2.2 × 3.2 |
| C9 | 113 × 162 | 40 × 57 | 1.6 × 2.2 |
| C10 | 79 × 113 | 28 × 40 | 1.1 × 1.6 |

### C.6 RA and SRA Paper Sizes

**Table C.7: RA and SRA Paper Sizes**

| MEDIA SIZE | SIZE IN POINTS | SIZE IN MILLIMETERS (NORMATIVE) | SIZE IN INCHES |
| --- | --- | --- | --- |
| RA0 | 2438 × 3458 | 860 × 1220 | 33.9 × 48.0 |
| RA1 | 1729 × 2438 | 610 × 860 | 24.0 × 33.9 |
| RA2 | 1219 × 1729 | 430 × 610 | 16.9 × 24.0 |
| RA3 | 865 × 1219 | 305 × 430 | 12.0 × 16.9 |
| RA4 | 609 × 865 | 215 × 305 | 8.5 × 12.0 |
| SRA0 | 2551 × 3628 | 900 × 1280 | 35.4 × 50.4 |
| SRA1 | 1814 × 2551 | 640 × 900 | 25.2 × 35.4 |
| SRA2 | 1276 × 1814 | 450 × 640 | 17.7 × 25.2 |
| SRA3 | 907 × 1276 | 320 × 450 | 12.6 × 17.7 |
| SRA4 | 638 × 907 | 225 × 320 | 8.9 × 12.6 |

### C.7 US ANSI Paper Sizes

**Table C.8: US ANSI Paper Sizes**

| MEDIA SIZE | SIZE IN POINTS | SIZE IN MILLIMETERS | SIZE IN INCHES (NORMATIVE) |
| --- | --- | --- | --- |
| AnsiA *a* | 612 × 792 | 215.9 × 279.4 | 8.5 × 11 |
| AnsiB *b* | 792 × 1224 | 279.4 × 431.8 | 11 × 17 |
| AnsiC | 1224 × 1584 | 431.8 × 558.8 | 17 × 22 |
| AnsiD | 1584 × 2448 | 558.8 × 863.6 | 22 × 34 |
| AnsiE | 2448 × 3168 | 863.6 × 1117.6 | 34 × 44 |

*a. Equivalent to US Letter.*
*b. Equivalent to US Ledger and US Tabloid.*

### C.8 US Paper Sizes

**Table C.9: US Paper Sizes**

| MEDIA SIZE | SIZE IN POINTS | SIZE IN MILLIMETERS | SIZE IN INCHES (NORMATIVE) |
| --- | --- | --- | --- |
| HalfLetter | 396 × 612 | 139.7 × 215.9 | 5.5 × 8.5 |
| Letter *a* | 612 × 792 | 215.9 × 279.4 | 8.5 × 11 |
| Legal | 612 × 1008 | 215.9 × 355.6 | 8.5 × 14 |
| JuniorLegal | 360 × 576 | 127.0 × 203.2 | 5 × 8 |
| Ledger *b* | 792 × 1224 | 279.4 × 431.8 | 11 × 17 |
| Tabloid *c* | 792 × 1224 | 279.4 × 431.8 | 11 × 17 |

*a. Equivalent to ANSI A.*
*b. Equivalent to ANSI B.*
*c. Equivalent to ANSI B.*
