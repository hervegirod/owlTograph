# ontologyBrowser
Allows to browse the content of an OWL/RDF ontology. The github repository where to look for the code is
at https://github.com/hervegirod/ontologyBrowser.


# History
## 0.2
 - Show the cardinality of the relations

## 0.3
 - Fix the manifest
 - Fix some cases where the parsing would lead to an exception

## 0.4
 - Use MDIUtilities 1.2.51
 - Use MDIFramework 1.3.11
 - Use jGraphml 1.2.4
 - Use docJGenerator 1.6.4.9
 - Fix some cases where the parsing would lead to an exception
 - Add an option to allow not to add the Thing class in the diagram
 - Add the notion of packages
 - Add a tree showing the packages and the classes on the left of the UI
 - Center the diagram when selecting a class

## 0.5
 - Use MDIUtilities 1.2.54
 - Use docJGenerator 1.6.5
 - Use Netbeans 12.5 for the development
 - Reorganize the libraries to put jena libraries in a specific folder
 - Add trees showing the properties and individuals on the left of the UI
 - Show the linked elements in a popup panel when right-clicking on the element in a tree
 - Show equivalent classes and equivalent properties
 - Add a search function
 - Allow parsing Turtle files
 - Protect the parser against unsupported Owl2 files