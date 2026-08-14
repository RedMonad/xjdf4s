# Appendix F

## F Hole Pattern Catalog

The following table defines the specifics of the predefined holes in `HoleMakingParams` and `HoleMakingIntent`.

**Notes:**
1. All patterns are centered on the sheet along the reference edge.
2. The reference edge is always defined relative to a portrait orientation of the medium, regardless of the orientation of the printed image or processing path.
3. The pattern axis offset is always specified relative to the reference edge.
4. The default pattern axis offset is always specified in points.
5. Thumbcuts are available in various standard shapes (labeled “No. N” where N is minimally ranging from 2..7). “No. 3” seems to be the most widely used.
6. Single thumbcuts appear always in the center of the reference edge.
7. Oval-shaped holes sometimes actually look more like rectangular holes with rounded corners.
8. A circle (○) in the Shape column denotes a round or elliptic hole.
9. A square (□) in the Shape column denotes a square or rectangular hole.
10. Generic hole types are dependent on the geographical area where the device is used.

### F.1 Naming Scheme

**Table F.1: Naming Scheme for Hole Patterns**

| NAME | DESCRIPTION |
| --- | --- |
| General | `<m\|i>`: m= metric(millimeter is used), i= imperial(inch, where 1 inch= 25.4 mm) |
| Ring Binding | `R<#holes><m\|i>-<variant>` Example: R2m-DIN= RingBind, 2 hole, metric, DIN |
| Plastic Comb | `P<pitch><m\|i>-<shape>-<#thumbcuts>t` Example: P16:9m-round-0t= Plastic Comb, 9/16" pitch(16:9), round, no thumbcut |
| Wire Comb | `W<pitch><m\|i>-<shape>-<#thumbcuts>t` Example: W2:1i-square-1t= Wire Comb, 1/2" pitch(2:1), square, one thumbcut |
| Coil/Spiral | `C<pitch><m\|i>-<shape>-<#thumbcuts>t` Example: C9.5m-round-0t= Coil, 9.5 mm, round, no thumbcut |
| Special | `S<#holes>` Example: S1-generic |

### F.2 Ring Binding- Two Hole

**Table F.2: Hole Details for R2 Series**

| HOLE PATTERN ID | DESCRIPTION | SHAPE | HOLE EXTENT | PATTERN GEOMETRY | PATTERN AXIS OFFSET | XJDF DEFAULT | EDGE | SOURCE |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| R2-generic | Generic request of a 2-hole pattern | ○ | 5- 13 mm | N/A | 4.5 – 13 mm | 34.02 (~ 12 mm) | Left | N/A |
| R2m-DIN | DIN 2-hole | ○ | 5.5 ± 0.1 mm | 80 ± 0.1 mm | 7 or 11 ± 0.3 mm | 31.18 (~ 11 mm) | Left | [DIN 5005] |
| R2m-ISO | ISO 2-hole | ○ | 6 ± 0.5 mm | 80 ± 0.5 mm | 12 ± 1 mm | 34.02 (~ 12 mm) | Left | [ISO838:1974] |
| R2m-MIB | Printer Finishing MIB | ○ | 5-8 mm | 80 ± 0.5 mm | 4.5 – 13 mm | 31.18 (~ 11 mm) | Left | [RFC3806] |
| R2i-US-a | US 2-hole, Variant A | ○ | 0.2-0.32" | 2.75" | 0.18- 0.51" | 29.25 (~ 13/32") | Left/Top | [RFC3806] |
| R2i-US-b | US 2-hole, Variant B | ○ | 0.2- 0.5" | 6" | 0.25"+ ½ diameter | 29.25 (~ 13/32") | Left | |

### F.3 Ring Binding- Three Hole

**Table F.3: Hole Details for R3 Series**

| HOLE PATTERN ID | DESCRIPTION | SHAPE | HOLE EXTENT | PATTERN GEOMETRY | PATTERN AXIS OFFSET | XJDF DEFAULT | EDGE | SOURCE |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| R3-generic | Generic request of a 3-hole pattern. | ○ | 5- 13 mm | N/A | 4.5 – 13 mm | 29.25 (~ 13/32") | Left | N/A |
| R3i-US | US 3-hole | ○ | 0.2- 0.5" | 4.25" | 0.25"+ ½ diameter | 29.25 (~ 13/32") | Left | [RFC3806] |

### F.4 Ring Binding- Four Hole

**Table F.4: Hole Details for R4 Series**

| HOLE PATTERN ID | DESCRIPTION | SHAPE | HOLE EXTENT | PATTERN GEOMETRY | PATTERN AXIS OFFSET | XJDF DEFAULT | EDGE | SOURCE |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| R4-generic | Generic request of a 4-hole pattern. | ○ | 5- 13 mm | N/A | 4.5 – 13 mm | 31.18 (~ 11 mm) | Left | N/A |
| R4m-DIN-A4 | DIN 4-hole for A4 | ○ | 5.5 ± 0.1 mm | 80 ± 0.1 mm | 7 or 11 ± 0.3 mm | 31.18 (~ 11 mm) | Left | [DIN 5005] |
| R4m-DIN-A5 | DIN 4-hole for A5 | ○ | 5.5 ± 0.1 mm | 45-65-45 mm | 7 or 11 ± 0.3 mm | 31.18 (~ 11 mm) | Left | [DIN 5005] |
| R4m-swedish | Swedish 4-hole | ○ | 5- 8 mm | 21-70-21 mm | 4.5- 13 mm | 31.18 (~ 11 mm) | Left/Top | [RFC3806] |
| R4i-US | US 4-hole | ○ | 0.2- 0.5" | 1.375-4.25-1.375" | 0.25"+ ½ diameter | 29.25 | Left | |

### F.5 RingBinding- Five Hole

**Table F.5: Hole Details for R5 Series**

| HOLE PATTERN ID | DESCRIPTION | SHAPE | HOLE EXTENT | PATTERN GEOMETRY | PATTERN AXIS OFFSET | XJDF DEFAULT | EDGE | SOURCE |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| R5-generic | Generic request of a 5-hole pattern. | ○ | 5- 13 mm | N/A | 4.5 – 13 mm | 29.25 (~ 13/32") | Left | N/A |
| R5i-US-a | US 5-hole, Variant A | ○ | 0.2-0.32" | 2-2.25-2.25-2" | 0.18- 0.51" | 29.25 (~ 13/32") | Left/Top | [RFC3806] |
| R5i-US-b | US 5-hole, Variant B | ○ | 0.2- 0.5" | 0.75-3.5-3.5-0.75" | 0.25"+ ½ diameter | 29.25 | Left | |
| R5i-US-c | Combination of R2i-US-a and R3i-US | ○ | 0.2- 0.5" | 1.25-3-3-1.25" | 0.25"+ ½ diameter | 29.25 | Left | |

### F.6 Ring Binding- Six Hole

**Table F.6: Hole Details for R6 Series**

| HOLE PATTERN ID | DESCRIPTION | SHAPE | HOLE EXTENT | PATTERN GEOMETRY | PATTERN AXIS OFFSET | XJDF DEFAULT | EDGE | SOURCE |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| R6-generic | Generic request of a 6-hole pattern. | ○ | 5- 13 mm | N/A | 4.5 – 13 mm | 31.18 (~ 11 mm) | Left/Top | N/A |
| R6m-4h2s | Norwegian 4-hole mixed with 2 slots | H:○ S:□ | H: 5-8 mm S: 10 × 5.5 mm | 64-18.5-75-18.5-64 mm | 4.5- 13 mm | 31.18 (~ 11 mm) | Left/Top | [RFC3806] |
| R6m-DIN-A5 | DIN 6-hole for A5 | ○ | 5.5 ± 0.1 mm | 37.5-7.5-65-7.5-37.5 mm | 7 or 11 ± 0.3 mm | 31.18 (~ 11 mm) | Left | [DIN 5005] |

### F.7 Ring Binding- Seven Hole

**Table F.7: Hole Details for R7 Series**

| HOLE PATTERN ID | DESCRIPTION | SHAPE | HOLE EXTENT | PATTERN GEOMETRY | PATTERN AXIS OFFSET | XJDF DEFAULT | EDGE | SOURCE |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| R7-generic | Generic request of a 7-hole pattern. | ○ | 5- 13 mm | N/A | 4.5 – 13 mm | 29.25 (~ 13/32") | Left/Top | N/A |
| R7i-US-a | US 7-hole, Variant A | ○ | 0.2-0.32" | 1-1-2.25-2.25-1-1" | 0.18- 0.51" | 29.25 (~ 13/32") | Left/Top | [RFC3806] |
| R7i-US-b | US 7-hole, Bell/AT&T Systems. | ○ | 0.2- 0.5" | 0.75-1.375-2.125-2.125-1.375-0.75" | 0.25"+ ½ diameter | 29.25 | Left/Top | |
| R7i-US-c | US 7-hole, Variant C | ○ | 0.2- 0.5" | 1.25-0.875-2.125-2.125-0.875-1.25" | 0.25"+ ½ diameter | 29.25 (~ 13/32") | Left/Top | |

### F.8 Ring Binding- Eleven Hole

**Table F.8: Hole Details for R11 Series**

| HOLE PATTERN ID | DESCRIPTION | SHAPE | HOLE EXTENT | PATTERN GEOMETRY | PATTERN AXIS OFFSET | XJDF DEFAULT | EDGE | SOURCE |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| R11m-7h4s | 7-hole(round) mixed with 4 slots | H:○ S:□ | H: 5-8 mm S: 12 × 6 mm | 15-25-23-20-37-37-20-23-25-15 mm | 4.5- 13 mm | 31.18 (~ 11 mm) | Left/Top | [RFC3806] |

### F.9 Plastic Comb Binding

**Table F.9: Hole Details for P Series**

| HOLE PATTERN ID | DESCRIPTION | SHAPE | HOLE EXTENT | PATTERN GEOMETRY | PATTERN AXIS OFFSET | XJDF DEFAULT | EDGE | SOURCE |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| P16_9i-rect-0t | US spacing, no thumbcut | □ | 5/16" × 1/8" | 9/16" | 3/16" | 13.54 (~ 0.188") | Left | [RFC3806] |
| P12m-rect-0t | European spacing, no thumbcut | □ | 7 × 3 mm | 12 mm | 4.5 mm | 12.76 (~ 4.5 mm) | Left | |

### F.10 Wire Comb Binding

Wire comb binding uses twenty-three holes for pages of A4 size, and twenty-one holes for pages of letter size.

**Table F.10: Hole Details for W Series**

| HOLE PATTERN ID | DESCRIPTION | SHAPE | HOLE EXTENT | PATTERN GEOMETRY | PATTERN AXIS OFFSET | XJDF DEFAULT | EDGE | SOURCE |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| W2_1i-round-0t | 2:1, round, no thumbcut | ○ | 0.2-0.32" | 1/2" | 3 mm+ ½ diameter | 17.50 (~ 0.243") | Left | [RFC3806] |
| W2_1i-square-0t | 2:1, square, no thumbcut | □ | 0.2-0.32" | 1/2" | 3 mm+ ½ diameter | 17.50 (~ 0.243") | Left | |
| W3_1i-square-0t | 3:1, square, no thumbcuts | □ | 5/32 × 5/32" | 1/3" | 0.2" | 14.40 (~ 0.2") | Left | |

### F.11 Coil and Spiral Binding

**Table F.11: Hole Details for C Series**

| HOLE PATTERN ID | DESCRIPTION | SHAPE | HOLE EXTENT | PATTERN GEOMETRY | PATTERN AXIS OFFSET | XJDF DEFAULT | EDGE | SOURCE |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| C9.5m-round-0t | 9.5 mm, round, no thumbcut | ○ | 5- 8 mm | 9.5 mm | 4.5- 13 mm | 31.18 (~ 11 mm) | Left/Top | [RFC3806] |

### F.12 Special Binding

**Table F.12: Hole Details for S Series**

| HOLE PATTERN ID | DESCRIPTION | SHAPE | HOLE EXTENT | PATTERN GEOMETRY | PATTERN AXIS OFFSET | XJDF DEFAULT | EDGE | SOURCE |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| S-generic | Generic request of a hole pattern with an arbitrary or unknown number of holes | ○ | 5- 13 mm | N/A | N/A | N/A | Any | N/A |
| S1-generic | Generic request of a hole pattern with 1 hole | ○ | 5- 13 mm | N/A | N/A | N/A | Any | N/A |
