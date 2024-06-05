/*
Copyright (c) 2023 Hervé Girod
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

3. Neither the name of the copyright holder nor the names of its
   contributors may be used to endorse or promote products derived from
   this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

Alternatively if you have any questions about this project, you can visit
the project website at the project page on https://github.com/hervegirod/ontologyBrowser
 */
package org.girod.ontobrowser.script;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.girod.ontobrowser.model.ElementKey;
import org.girod.ontobrowser.model.OwlClass;
import org.girod.ontobrowser.model.OwlProperty;
import org.girod.ontobrowser.model.ElementFilter;
import org.girod.ontobrowser.model.OwlIndividual;
import org.girod.ontobrowser.model.OwlSchema;
import org.girod.ontobrowser.utils.SchemaUtils;
import org.scripthelper.context.ScriptContext;
import org.scripthelper.context.ScriptHelper;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The script helper.
 *
 * @version 0.13
 */
public class OwlScriptHelper implements ScriptHelper {
   private final OwlScriptContext context;
   private final OwlSchema schema;
   private final OntModel ontModel;

   public OwlScriptHelper(OwlScriptContext context) {
      this.context = context;
      this.schema = context.getSchema();
      this.ontModel = schema.getOntModel();
   }

   @Override
   public ScriptContext getContext() {
      return context;
   }

   /**
    * Return the data properties of a Class.
    *
    * @param theClass the Class
    * @param filter the request filter
    * @return the data properties
    */
   public static Map<ElementKey, OwlProperty> getDataProperties(OwlClass theClass, ElementFilter filter) {
      return SchemaUtils.getDataProperties(theClass, filter);
   }

   /**
    * Return the properties for which this Class is in their domain.
    *
    * @param theClass the Class
    * @param filter the request properties
    * @return the properties for which this Class is in their domain
    */
   public static Map<ElementKey, OwlProperty> getDomainProperties(OwlClass theClass, ElementFilter filter) {
      return SchemaUtils.getDomainProperties(theClass, filter);
   }

   /**
    * Return the properties for which this Class is in their range.
    *
    * @param theClass the Class
    * @param filter the request filter
    * @return the properties for which this Class is in their range
    */
   public Map<ElementKey, OwlProperty> getRangeProperties(OwlClass theClass, ElementFilter filter) {
      return SchemaUtils.getRangeProperties(theClass, filter);
   }

   /**
    * Return the classes dependant from a class.
    *
    * @param theClass the class
    * @param filter the filter
    * @return the dependant classes
    */
   public static Map<ElementKey, OwlClass> getDependentClasses(OwlClass theClass, ElementFilter filter) {
      return SchemaUtils.getDependentClasses(theClass, filter);
   }

   /**
    * Open an XML file.
    *
    * @param desc the descrption of the dialog
    * @return the file (or null if the dialog was aborted)
    */
   public File openXMLFile(String desc) {
      ScriptXMLFileDialog dialog = new ScriptXMLFileDialog(context.getApplication().getApplicationWindow(), desc, context);
      int ret = dialog.showDialog();
      if (ret == JFileChooser.APPROVE_OPTION) {
         return dialog.getSelectedFile();
      } else {
         return null;
      }
   }

   public ElementKey getKeyFromDefaultNamespace(String name) {
      return ElementKey.create(schema.getDefaultNamespace(), name);
   }

   /**
    * Add an individual, for a class in the default namespace.
    *
    * @param className the class name
    * @param name the individual name
    * @return the individual, or null if it was not psosible to create the individual
    */
   public OwlIndividual addIndividual(String className, String name) {
      ElementKey classKey = getKeyFromDefaultNamespace(className);
      return addIndividual(classKey, name);
   }

   /**
    * Add an individual.
    *
    * @param classKey the class key
    * @param name the individual name
    * @return the individual, or null if it was not psosible to create the individual
    */
   public OwlIndividual addIndividual(ElementKey classKey, String name) {
      if (schema.hasOwlClass(classKey)) {
         ElementKey individualKey = ElementKey.create(classKey.getNamespace(), name);
         if (schema.hasIndividual(individualKey)) {
            return null;
         }
         OwlClass owlClass = schema.getOwlClass(classKey);
         // see https://stackoverflow.com/questions/43719469/create-individuals-using-jena
         Individual individual = ontModel.createIndividual(individualKey.getPrefixedName(schema), owlClass.getOntClass());
         OwlIndividual owlIndividual = new OwlIndividual(owlClass, individual);
         return owlIndividual;
      } else {
         return null;
      }
   }
   
   public boolean addIndividualPropertyValue(ElementKey individualKey,  ElementKey propertyKey, String value) {
      if (schema.hasIndividual(individualKey)) {
         if (schema.hasOwlProperty(propertyKey)) {
            OwlProperty owlproperty = schema.getOwlProperty(propertyKey);
            Property property = owlproperty.getProperty();
            OwlIndividual owlIndividual = schema.getIndividual(individualKey);
            Resource resource = owlIndividual.getIndividual();
            resource.addProperty(property, value);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }   

   public boolean addIndividualPropertyValue(ElementKey individualKey, String propertyName, String value) {
      ElementKey propertyKey = ElementKey.create(individualKey.getNamespace(), propertyName);
      return addIndividualPropertyValue(individualKey, propertyKey, value);
   }

   /**
    * Parse an XML file.
    *
    * @param file the XML file
    * @param xmlHandler the XML handler
    */
   public void parseXML(File file, XMLHandler xmlHandler) {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      try {
         SAXParser parser = factory.newSAXParser();
         XMLParserHandler phandler = new XMLParserHandler(xmlHandler);
         parser.parse(file, phandler);
      } catch (ParserConfigurationException | SAXException | IOException ex) {
         xmlHandler.fireException(ex);
      }
   }

   private class XMLParserHandler extends DefaultHandler {
      private final XMLHandler xmlHandler;

      private XMLParserHandler(XMLHandler xmlHandler) {
         this.xmlHandler = xmlHandler;
      }

      @Override
      public void startElement(String uri, String localName, String qName, Attributes attr) {
         Map<String, String> attributes = new HashMap<>();
         for (int i = 0; i < attr.getLength(); i++) {
            String attrname = attr.getQName(i);
            String attrvalue = attr.getValue(i);
            attributes.put(attrname, attrvalue);
         }
         xmlHandler.startElement(qName, attributes);
      }

      @Override
      public void endElement(String uri, String localName, String qName) {
         xmlHandler.endElement(localName);
      }

      @Override
      public void error(SAXParseException e) {
         xmlHandler.error(e);
         context.echo(e.getMessage(), "red");
      }

      @Override
      public void warning(SAXParseException e) {
         xmlHandler.warning(e);
         context.echo(e.getMessage(), "red");
      }

      @Override
      public void fatalError(SAXParseException e) {
         context.echo(e.getMessage(), "red");
         xmlHandler.fatalError(e);
      }
   }
}