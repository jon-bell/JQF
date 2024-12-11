import sys
import json

bms = [
{"id":"ant", "class":"edu.berkeley.cs.jqf.examples.ant.ProjectBuilderTest", "method" : "testWithGenerator",
"coveragePackages" : "org/apache/tools/ant/*"},
{"id":"bcel", "class":"edu.berkeley.cs.jqf.examples.bcel.ParserTest", "method" : "testWithGenerator",
"coveragePackages" : "org/apache/bcel/*"},
{"id":"closure", "class" : "edu.berkeley.cs.jqf.examples.closure.CompilerTest", "method": "testWithGenerator",
"coveragePackages" : "com/google/javascript/jscomp/*"},
{"id":"maven", "class" : "edu.berkeley.cs.jqf.examples.maven.ModelReaderTest", "method" : "testWithGenerator",
"coveragePackages" : "org/apache/maven/model/*"},
{"id":"rhino", "class" : "edu.berkeley.cs.jqf.examples.rhino.CompilerTest", "method" : "testWithGenerator",
"coveragePackages" : "org/mozilla/javascript/optimizer/*:org/mozilla/javascript/CodeGenerator*"},
# {"id":"chocopy", "class" : "edu.berkeley.cs.jqf.examples.chocopy.SemanticAnalysisTest", "method" : "fuzzSemanticAnalysis",
# "coveragePackages": "chocopy/*"},
# {"id": "nashorn", "class": "edu.berkeley.cs.jqf.examples.nashorn.CompilerTest", "method": "testWithGenerator",
# "coveragePackages": "jdk/nashorn/*"},
# {"id": "pngj", "class": "edu.berkeley.cs.jqf.examples.pngj.PngReaderTest", "method": "testWithGenerator",
# "coveragePackages": "ar/com/hjg/pngj/*"},
# {"id": "tomcat", "class": "edu.berkeley.cs.jqf.examples.tomcat.WebXmlTest", "method": "testWithGenerator",
# "coveragePackages": "org/apache/tomcat/*"}
]

if(len(sys.argv) == 2):
    configs = []
    for x in range(int(sys.argv[1])):
        for bm in bms:
            tmp = bm.copy()
            tmp['runNumber'] = x
            configs.append(tmp)
    print(json.dumps({'config':configs}))
else:
    print(json.dumps({'config': bms}))
