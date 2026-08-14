# Appendix E

## E Pagination Catalog

This appendix provides a set of diagrams that explain how pages are arranged in groups when preparing to print on the surfaces of large sheets. The diagrams show a wide range of folding patterns to be used before binding. The folding patterns are specified in the XJDF Fold Catalog, which describes how to paginate single-sheet bindery signatures.

### E.1 How to interpret the diagrams

#### E.1.1 Legend
This appendix describes the structure and arrangement of bindery signatures into pagination schemes, which divide sheet surfaces into grids of rectangular areas to be filled by pages during the imposition process.
Each diagram shows a side of an unfolded sheet, illustrating how it is divided into "signature cells". A signature cell is the space that "receives" a single document page and surrounding margins that are part of the gutters.
*Note: In contrast to the usual convention in this specification that all indices are ‘zero’ based, the page numbering in the diagrams in this appendix are ‘one’ based for readability.*

*[Изображение: Легенда для интерпретации схем (Figure E-1). Показывает обозначения верхнего левого угла передней стороны буклета, края переплета (Binding Edge), края Jog Edge, ориентацию верха страницы и индексы пагинации на лицевой и оборотной сторонах листа.]*

#### E.1.2 Meaning of a Pagination Scheme
The pagination indexes shown in the diagram correspond to the imposition order, starting with 1, up to the number of pages in a booklet. These numbers specify the order that pages are imposed into signature cells, from an array of pages associated with a booklet.

#### E.1.3 Modifying the Pagination Schemes with BindingOrientation
`BinderySignature/@BindingOrientation` MAY be set to indicate that the reference corner SHALL be displaced. This modifies the location of the spine, head, face and foot on the booklet before pagination is applied, e.g. for binding calendars or books to allow for a right to left reading order.
*Important note: When a page is rotated 90° (clockwise or counterclockwise), this rotation is made inside the signature cell. The cell itself is not rotated because the folding operation remains the same.*

#### E.1.4 Examples of applying BindingOrientation
*[Изображение: Схемы (Tables E.1 - E.4), демонстрирующие трансформацию раскладки страниц (импозиции) при применении различных значений @BindingOrientation (Rotate0, Rotate90, Flip0 и т.д.) для горизонтальных и вертикальных финальных фальцев. Числа в сетках показывают итоговый порядок страниц.]*

### E.2 Pagination Diagrams

**Table E.5: Pagination Diagrams**

*Примечание: В оригинальном документе каждая строка данной таблицы сопровождается визуальной схемой раскладки страниц (импозиции). На схемах изображены сетки ячеек сигнатуры с номерами страниц (от 1 до N), которые показывают порядок наложения, ориентацию и положение фальцев для чтения готового буклета. Ниже приведены параметры каталога фальцовки.*

| FOLD CATALOG | GRID SIZE | DESCRIPTION | FOLDING SEQUENCE |
| --- | --- | --- | --- |
| F2-1 | 1 × 1 | (No folding sequence) | - |
| F4-1 | 2 × 1 | | 1/2 |
| F4-2 | 2 × 1 | | 1/2 |
| F6-1 | 3 × 1 | | 1/3 1/3 |
| F6-2 | 3 × 1 | | 1/3 1/3 |
| F6-3 | 3 × 1 | Unsupported (gatefold) | 1/4 1/2 |
| F6-4 | 3 × 1 | | 1/3 1/3 |
| F6-5 | 3 × 1 | | 2/3 1/3 |
| F6-6 | 3 × 1 | Unsupported (multiple page sizes) | 3/4 1/4 |
| F6-7 | 3 × 1 | Unsupported (multiple page sizes) | 1/4 1/4 |
| F6-8 | 3 × 1 | | 2/3 1/3 |
| F8-1 | 4 × 1 | | 1/2 1/4 |
| F8-2 | 4 × 1 | | 1/2 1/4 |
| F8-3 | 4 × 1 | | 1/4 1/4 1/4 |
| F8-4 | 4 × 1 | | 1/4 1/2 1/4 |
| F8-5 | 4 × 1 | | 1/4 1/4 1/4 |
| F8-6 | 4 × 1 | | 3/4 1/4 1/4 |
| F8-7 | 2 × 2 | | 1/2 + 1/2 |
| F10-1 | 5 × 1 | | 1/5 1/5 1/5 1/5 |
| F10-2 | 5 × 1 | | 4/5 1/5 1/5 1/5 |
| F10-3 | 5 × 1 | | 2/5 2/5 1/5 |
| F12-1 | 6 × 1 | | 1/3 1/3 1/6 |
| F12-2 | 6 × 1 | | 1/3 1/3 1/6 |
| F12-3 | 6 × 1 | | 1/2 1/6 1/6 |
| F12-4 | 6 × 1 | | 1/2 1/6 1/6 |
| F12-5 | 6 × 1 | | 1/2 1/3 1/6 |
| F12-6 | 6 × 1 | | 1/6 1/6 1/6 1/6 1/6 |
| F12-7 | 3 × 2 | | 1/3 1/3 + 1/2 |
| F12-8 | 3 × 2 | | 2/3 1/3 + 1/2 |
| F12-9 | 3 × 2 | | 1/3 1/3 + 1/2 |
| F12-10 | 3 × 2 | | 2/3 1/3 + 1/2 |
| F12-11 | 3 × 2 | | 1/3 + 1/2 + 1/3 |
| F12-12 | 2 × 3 | | 1/2 + 2/3 1/3 |
| F12-13 | 2 × 3 | | 1/2 + 1/3 1/3 |
| F12-14 | 2 × 3 | | 1/2 + 1/3 1/3 |
| F14-1 | 7 × 1 | | 1/7 1/7 1/7 1/7 1/7 1/7 |
| F16-1 | 8 × 1 | | 1/2 1/4 1/8 |
| F16-2 | 8 × 1 | | 1/2 1/4 1/8 |
| F16-3 | 8 × 1 | | 1/2 1/4 1/8 |
| F16-4 | 8 × 1 | | 1/2 1/4 1/8 |
| F16-5 | 8 × 1 | | 1/8 1/8 1/8 1/8 1/8 1/8 1/8 |
| F16-6 | 4 × 2 | | 1/2 + 1/2 + 1/4 |
| F16-7 | 4 × 2 | | 1/2 + 1/2 + 1/4 |
| F16-8 | 4 × 2 | | 1/2 + 1/2 + 1/4 |
| F16-9 | 4 × 2 | | 1/2 1/4 + 1/2 |
| F16-10 | 4 × 2 | | 1/2 1/4 + 1/2 |
| F16-11 | 4 × 2 | | 1/4 1/4 1/4 + 1/2 |
| F16-12 | 4 × 2 | | 1/4 1/4 1/4 + 1/2 |
| F16-13 | 2 × 4 | | 1/2 + 1/2 1/4 |
| F16-14 | 2 × 4 | | 1/2 + 1/2 1/4 |
| F18-1 | 9 × 1 | | 1/9 1/9 1/9 1/9 1/9 1/9 1/9 1/9 |
| F18-2 | 9 × 1 | | 2/3 1/3 1/9 1/9 |
| F18-3 | 9 × 1 | | 1/3 1/3 2/9 1/9 |
| F18-4 | 9 × 1 | | 1/3 1/3 1/9 1/9 |
| F18-5 | 3 × 3 | | 1/3 1/3 + 1/3 1/3 |
| F18-6 | 3 × 3 | | 1/3 1/3 + 2/3 1/3 |
| F18-7 | 3 × 3 | | 1/3 1/3 + 1/3 1/3 |
| F18-8 | 3 × 3 | | 1/3 1/3 + 2/3 1/3 |
| F18-9 | 3 × 3 | | 2/3 1/3 + 2/3 1/3 |
| F20-1 | 5 × 2 | | 2/5 2/5 1/5 + 1/2 |
| F20-2 | 5 × 2 | | 1/5 1/5 1/5 1/5 + 1/2 |
| F24-1 | 6 × 2 | | 1/3 1/3 + 1/2 + 1/6 |
| F24-2 | 6 × 2 | | 1/3 1/3 + 1/2 + 1/6 |
| F24-3 | 6 × 2 | | 1/3 1/3 1/6 + 1/2 |
| F24-4 | 6 × 2 | | 1/3 1/3 1/6 + 1/2 |
| F24-5 | 6 × 2 | | 1/3 1/3 1/6 + 1/2 |
| F24-6 | 6 × 2 | | 1/6 1/6 1/6 1/6 1/6 + 1/2 |
| F24-7 | 6 × 2 | | 1/3 + 1/2 + 1/3 1/6 |
| F24-8 | 3 × 4 | | 1/3 1/3 + 1/2 1/4 |
| F24-9 | 3 × 4 | | 2/3 1/3 + 1/2 1/4 |
| F24-10 | 3 × 4 | | 1/3 1/3 + 1/2 1/4 |
| F24-11 | 4 × 3 | | 1/2 + 2/3 1/3 + 1/4 |
| F28-1 | 7 × 2 | | 1/7 1/7 1/7 1/7 1/7 1/7 + 1/2 |
| F32-1 | 16 × 1 | | 1/2 1/4 1/8 1/16 |
| F32-2 | 8 × 2 | | 1/2 1/4 + 1/2 + 1/8 |
| F32-3 | 8 × 2 | | 1/2 1/4 + 1/2 + 1/8 |
| F32-4 | 4 × 4 | | 1/2 + 1/2 + 1/4 + 1/4 |
| F32-5 | 4 × 4 | | 1/2 + 1/2 + 1/4 + 1/4 |
| F32-6 | 4 × 4 | | 1/2 + 1/2 + 1/4 + 1/4 |
| F32-7 | 4 × 4 | | 1/4 1/4 1/4 + 1/2 1/4 |
| F32-8 | 4 × 4 | | 1/2 1/4 + 1/2 1/4 |
| F32-9 | 4 × 4 | | 1/2 + 1/2 1/4 + 1/4 |
| F36-1 | 9 × 2 | | 1/3 1/3 1/9 1/3 + 1/2 |
| F36-2 | 6 × 3 | | 1/3 1/3 + 1/3 1/3 + 1/6 |
| F40-1 | 5 × 4 | | 1/5 1/5 1/5 1/5 + 1/2 1/4 |
| F48-1 | 6 × 4 | | 1/3 1/3 + 1/4 1/4 1/4 + 1/6 |
| F48-2 | 4 × 6 | | 1/4 1/4 1/4 + 1/3 1/3 1/6 |
| F64-1 | 8 × 4 | | 1/2 + 1/4 1/4 1/4 + 1/4 1/8 |
| F64-2 | 8 × 4 | | 1/4 1/4 1/4 + 1/4 1/4 1/4 + 1/8 |
