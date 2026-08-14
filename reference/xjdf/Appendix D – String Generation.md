# Appendix D

## D String Generation

XJDF specifies pairs of attributes that allow for the dynamic generation of strings. Each pair comprises of a format and a template named `@XXXFormat` `@XXXTemplate` where the ‘XXX’ is a generic place holder used for convenience in this chapter. For example `FileSpec` has a pair to allow for the automatic generation of file names called, `@FileFormat` and `@FileTemplate`.

The function defined when using the attributes `@XXXFormat` and `@XXXTemplate` is based on the standard C `printf()` function. (See [K&R].) `@XXXFormat` is the first argument and `@XXXTemplate` is a list of values selected from Table D.1 Template Variables.

### D.1 Template Variables

The following table describes predefined variables used in `@XXXTemplate` values.

**Table D.1: Template Variables**

| NAME | ‘C’ LANGUAGE DATA TYPE | DESCRIPTION |
| --- | --- | --- |
| `<a partition key>` | string | Any Partition Key that is an attribute of Part; see Table 6.4 Part Element. |
| ActualAmount | float | Actual amount of the product that was produced. |
| Amount | float | Planned amount of the product that was produced. |
| CustomerID | string | CustomerInfo/@CustomerID. |
| CustomerName | string | Contact/Person of the customer contact. The sequence and selection of attributes is system dependent. |
| Date | string | Current date in [ISO8601:2004] format. |
| DeviceID | string | Device/@DeviceID of the device that produced the output. |
| DeviceName | string | Device/@DescriptiveName of the device that produced the output. |
| EndTime | string | Actual end time of the job. |
| Error | string | List of errors that occurred during the job. The formatting of errors is system dependent. |
| ErrorStats | string | Statistics on errors that happened during execution. The formatting of error statistics is system dependent. |
| ExposedMediaName | string | Resource/@DescriptiveName of the plate that is being imaged. |
| GeneralID:XXX | string | GeneralID/@IDValue of a GeneralID[@IDUsage="XXX"]. |
| Generated | string | System generated string, for example a file name. |
| Input | string | Local file name of the input file. A value of "Input" SHALL NOT be specified for a FileSpec that describes an input file. |
| JobID | string | XJDF/@JobID of the job. |
| JobName | string | XJDF/@DescriptiveName of the job that is being processed. |
| JobPartID | string | XJDF/@JobPartID of the job. |
| MediaBrand | string | Resource/@Brand of the media that is being printed. |
| MoonPhase | string | Phase of the moon at the @StartTime of the job. |
| Operator | string | Contact/Person that describes the operator. The sequence and selection of attributes is system dependent. |
| OperatorText | string | Text from the operator as defined in Comment[@Type="OperatorText"]. |
| PressProfileName | string | The value of ColorSpaceConversionParams/FileSpec/@UserFileName of the ColorSpaceConversion process that is used for final output on the press. |
| PrintQuality | string | The value of PrintCondition/@PrintQuality. |
| ProoferProfileName | string | The value of ColorSpaceConversionParams/FileSpec/@UserFileName of the ColorSpaceConversion process that is used for proofing. |
| Resolution | int | The value of ObjectResolution/@Resolution. |
| ResolutionX | int | The first (X) value of ObjectResolution/@Resolution. |
| ResolutionY | int | The first (Y) value of ObjectResolution/@Resolution. |
| ScreeningFamily | string | The value of ScreeningParams/ScreenSelector/@ScreeningFamily. |
| StartTime | string | Actual start time of the job. |
| Time | string | Current time in [ISO8601:2004] format. |
| TotalPagesInDoc | int | Value of RunList/@NPage of the current document. |
| Warning | string | Warnings that happened during the job. Warnings don't lose information in the resulting job, while errors do. The formatting of warnings is system dependent. |

**Example D.1: @FileTemplate and @FileFormat**
With `@JobID="j001"` and a RunList defining 2024 created files, this example will iterate over all created files and place them into:
`"file://myserver/next/j001/m0000.pdf"` ... `"file://myserver/next/j001/m2023.pdf"`

```xml
<RunList>
  <FileSpec FileFormat="file://myserver/next/%s/m%4.i.pdf" FileTemplate="JobID DocIndex" MimeType="application/pdf"/>
</RunList>
```

### D.2 Template Operators

Numerical variables, i.e. variables with an entry of float or int in the ‘C’ LANGUAGE DATA TYPE column of Table D.1 Template Variables MAY be modified with explicit numbers and simple mathematical operators as defined in Table D.2 Template Operators and SHALL be evaluated using standard C-operator precedence.

**Table D.2: Template Operators**

| OPERATOR | NOTES |
| --- | --- |
| + | Addition. Both unary and binary addition SHALL be supported. |
| - | Subtraction. Both unary and binary subtraction SHALL be supported. |
| * | Multiplication. |
| / | Division. |
| % | Modulo. |
| () | Parentheses. |
