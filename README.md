# Visual GCode Editor & CAD
A java (Netbeans project) implementation of a Wysiwyg GCODE editor and 2D-CAD.

For laser engraver and simple milling projects.

Written to use my home made CNC.
Work with GRBL Arduino (with multiple configurations)

With some interesting things :
- 2D Edition, Bezier curve, circle, paths, groups, etc.
- basic 2D edition (rotation, scaling, translation, join, inversion)
- 2D & 3D Pockets,
- Text on existing path,
- import/export to DXF, SVG, and GCODE formats,
- export to OpenScad (by clipboard) 
- Realtime execution with GRBL

It use some external library (all included in jar releases) :
- RXTXcomm.jar
- kabeja-0.4.jar
- exp4j-0.4.8.jar

It is my personal project that I share without any guarantee, it is absolutely not finished, and will never be finished.

Take what you want from this and enjoy to adapt it to your needs, but be carful it is certainly buggy (the clipboard for example)

![A screenshot of the application](https://github.com/clmwith/visualgcodeeditor/blob/main/screenshot-demo.png)