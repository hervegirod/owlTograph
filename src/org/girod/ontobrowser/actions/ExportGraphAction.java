/*
Copyright (c) 2021, 2022, 2023 Hervé Girod
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
package org.girod.ontobrowser.actions;

import java.awt.Color;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.girod.jgraphml.GraphMLFactory;
import org.girod.jgraphml.model.Arrows;
import org.girod.jgraphml.model.DiagramDefaults;
import org.girod.jgraphml.model.EdgeLabel;
import org.girod.jgraphml.model.GraphMLDiagram;
import org.girod.jgraphml.model.GraphMLEdge;
import org.girod.jgraphml.model.GraphMLGroupNode;
import org.girod.jgraphml.model.GraphMLNode;
import org.girod.jgraphml.model.NodeLabel;
import org.girod.jgraphml.model.ShapeType;
import org.girod.ontobrowser.BrowserConfiguration;
import org.girod.ontobrowser.OwlDiagram;
import org.girod.ontobrowser.model.ElementKey;
import org.girod.ontobrowser.model.OwlClass;
import org.girod.ontobrowser.model.OwlDatatype;
import org.girod.ontobrowser.model.OwlDatatypeProperty;
import org.girod.ontobrowser.model.OwlIndividual;
import org.girod.ontobrowser.model.OwlObjectProperty;
import org.girod.ontobrowser.model.OwlProperty;
import org.girod.ontobrowser.model.OwlSchema;
import org.mdi.bootstrap.MDIApplication;
import org.mdi.bootstrap.swing.AbstractMDIAction;

/**
 * The Action that save schemas as yEd diagrams.
 *
 * @version 0.4
 */
public class ExportGraphAction extends AbstractMDIAction {
   private static final String DEFAULT_NS = "http://www.w3.org/2001/XMLSchema#";
   private static final String LIGHT_GREEN = "#90EE90";
   private File file = null;
   private OwlDiagram diagram = null;
   private GraphMLDiagram graph;
   private boolean showRelationsConstraints = false;
   private boolean showDataPropertiesTypes = false;
   private boolean showPackages = false;
   private OwlSchema schema;
   private Map<ElementKey, GraphMLGroupNode> packagesNodes = new HashMap<>();
   private DiagramDefaults defaults = null;

   /**
    * Create the export File Action.
    *
    * @param app the Application
    * @param desc the short description of the action
    * @param longDesc the long description of the action
    * @param diagram the diagram
    * @param file the file to open
    */
   public ExportGraphAction(MDIApplication app, String desc, String longDesc, OwlDiagram diagram, File file) {
      super(app, "Export Graph");
      this.file = file;
      this.diagram = diagram;
      this.setDescription(desc, longDesc);
   }

   private GraphMLGroupNode getPackageNode(OwlClass theClass, ElementKey key) {
      OwlClass superClass = theClass.getFirstSuperClass();
      if (superClass == null) {
         GraphMLGroupNode node = graph.addGroupNode();
         setGroupNodeStyle(node, theClass.getName());
         packagesNodes.put(key, node);
         return node;
      } else {
         ElementKey superclassKey = superClass.getKey();
         if (packagesNodes.containsKey(superclassKey)) {
            GraphMLGroupNode superclassNode = packagesNodes.get(superclassKey);
            GraphMLGroupNode node = superclassNode.addGroupNode();
            setGroupNodeStyle(node, theClass.getName());
            packagesNodes.put(key, node);
            return node;
         } else {
            GraphMLGroupNode superclassNode = getPackageNode(schema.getOwlClass(superclassKey), superclassKey);
            GraphMLGroupNode node = superclassNode.addGroupNode();
            setGroupNodeStyle(node, theClass.getName());
            packagesNodes.put(key, node);
            return node;
         }
      }
   }

   private void setGroupNodeStyle(GraphMLGroupNode node, String name) {
      node.setType(ShapeType.ROUNDRECTANGLE);
      node.setFillColor(LIGHT_GREEN);
      NodeLabel label = node.setLabel(name);
      label.setFontSize(14);
   }

   private GraphMLGroupNode getGroupParent(ElementKey key, OwlClass owlClass) {
      if (packagesNodes == null || !owlClass.isInPackage()) {
         return null;
      } else {
         ElementKey thePackageKey = owlClass.getPackage();
         return packagesNodes.get(thePackageKey);
      }
   }

   private GraphMLNode addIndividual(OwlClass owlClass, GraphMLNode node, OwlIndividual individual) {
      GraphMLNode inode;
      if (owlClass.isInPackage()) {
         ElementKey packageKey = owlClass.getPackage();
         GraphMLGroupNode groupNode = packagesNodes.get(packageKey);
         inode = groupNode.addNode();
      } else {
         inode = graph.addNode();
      }
      inode.getShapeNode().setType(ShapeType.ROUNDRECTANGLE);
      inode.getShapeNode().setFillColor(Color.MAGENTA);
      NodeLabel ilabel = inode.createLabel(true);
      ilabel.setFontSize(11);
      ilabel.setLabel(individual.getName());
      GraphMLEdge edge = graph.addEdge(node, inode);
      Arrows arrows = edge.getArrows();
      arrows.setSource(Arrows.NONE);
      arrows.setTarget(Arrows.NONE);
      return inode;
   }

   private GraphMLNode addDataProperty(OwlClass owlClass, OwlDatatypeProperty dataProperty) {
      GraphMLNode propertyNode;
      if (owlClass.isInPackage()) {
         ElementKey packageKey = owlClass.getPackage();
         GraphMLGroupNode groupNode = packagesNodes.get(packageKey);
         propertyNode = groupNode.addNode();
      } else {
         propertyNode = graph.addNode();
      }
      propertyNode.getShapeNode().setType(ShapeType.ROUNDRECTANGLE);
      propertyNode.getShapeNode().setFillColor(Color.CYAN);
      NodeLabel label = propertyNode.createLabel(true);
      label.setFontSize(11);
      if (showDataPropertiesTypes) {
         String typeS = getType(dataProperty);
         if (typeS != null) {
            label.setLabel(dataProperty.getName() + "\n" + typeS);
            propertyNode.setHeight(propertyNode.getHeight() * 1.5f);
         } else {
            label.setLabel(dataProperty.getName());
         }
      } else {
         label.setLabel(dataProperty.getName());
      }
      return propertyNode;
   }

   private GraphMLNode addLeafNode(Map<ElementKey, GraphMLNode> elementToNode, ElementKey key, OwlClass owlClass) {
      GraphMLGroupNode groupNode = getGroupParent(key, owlClass);
      GraphMLNode node;
      if (groupNode == null) {
         node = graph.addNode();
      } else {
         node = groupNode.addNode();
      }
      node.getShapeNode().setType(ShapeType.ROUNDRECTANGLE);
      node.getShapeNode().setFillColor(Color.LIGHT_GRAY);
      NodeLabel label = node.createLabel(true);
      label.setFontSize(11);
      label.setLabel(owlClass.getName());
      elementToNode.put(key, node);

      // individuals
      if (owlClass.hasIndividuals()) {
         Iterator<OwlIndividual> it2 = owlClass.getIndividuals().values().iterator();
         while (it2.hasNext()) {
            OwlIndividual individual = it2.next();
            addIndividual(owlClass, node, individual);
         }
      }
      return node;
   }

   @Override
   public void run() throws Exception {
      packagesNodes = new HashMap<>();
      BrowserConfiguration conf = BrowserConfiguration.getInstance();
      this.showRelationsConstraints = conf.showRelationsConstraints;
      this.showDataPropertiesTypes = conf.showDataPropertiesTypes;
      this.showPackages = diagram.hasPackages();

      GraphMLFactory factory = GraphMLFactory.getInstance();
      graph = factory.newDiagram();
      defaults = graph.getDefaults();
      defaults.edgeLabelAutoRotate = true;
      defaults.edgeLabelAutoFlip = true;
      defaults.nodeFontSize = 11;
      defaults.edgeFontSize = 11;
      defaults.autosized = true;
      defaults.padWidth = conf.padWidth;
      defaults.padHeight = conf.padHeight;
      defaults.edgeLabelDistance = -4f;
      defaults.edgeLabelPosition = EdgeLabel.ParamModel.POSITION_TAIL;
      defaults.arrowSource = Arrows.NONE;
      defaults.arrowTarget = Arrows.STANDARD;

      Map<ElementKey, GraphMLNode> elementToNode = new HashMap<>();
      schema = diagram.getSchema();

      Map<ElementKey, OwlClass> owlClasses = schema.getOwlClasses();
      if (showPackages) {
         Iterator<Entry<ElementKey, OwlClass>> it = owlClasses.entrySet().iterator();
         while (it.hasNext()) {
            Entry<ElementKey, OwlClass> entry = it.next();
            ElementKey key = entry.getKey();
            OwlClass owlClass = entry.getValue();
            if (schema.isPackage(key)) {
               getPackageNode(owlClass, key);
            }
         }
      }

      Iterator<Entry<ElementKey, OwlClass>> it = owlClasses.entrySet().iterator();
      while (it.hasNext()) {
         Entry<ElementKey, OwlClass> entry = it.next();
         ElementKey key = entry.getKey();
         OwlClass owlClass = entry.getValue();
         if (!showPackages || !packagesNodes.containsKey(key)) {
            addLeafNode(elementToNode, key, owlClass);
         }
      }

      // parent classes
      Iterator<ElementKey> it2 = owlClasses.keySet().iterator();
      while (it2.hasNext()) {
         ElementKey key = it2.next();
         OwlClass theClass = owlClasses.get(key);
         if (!showPackages || !packagesNodes.containsKey(key)) {
            GraphMLNode theNode = elementToNode.get(key);
            Iterator<ElementKey> it3 = theClass.getSuperClasses().keySet().iterator();
            while (it3.hasNext()) {
               ElementKey parentKey = it3.next();
               if (owlClasses.containsKey(parentKey)) {
                  OwlClass parentClass = owlClasses.get(parentKey);
                  if (!showPackages || !parentClass.isPackage()) {
                     GraphMLNode source = elementToNode.get(parentKey);
                     GraphMLEdge edge = graph.addEdge(source, theNode);
                     Arrows arrows = edge.getArrows();
                     arrows.setSource(Arrows.WHITE_DELTA);
                     arrows.setTarget(Arrows.NONE);
                  }
               }
            }
         }
      }

      // properties
      it2 = owlClasses.keySet().iterator();
      while (it2.hasNext()) {
         ElementKey key = it2.next();
         OwlClass theClass = owlClasses.get(key);
         GraphMLNode theNode = elementToNode.get(key);
         Iterator<OwlProperty> it3 = theClass.getOwlProperties().values().iterator();
         while (it3.hasNext()) {
            OwlProperty property = it3.next();
            if (property instanceof OwlObjectProperty) {
               OwlObjectProperty objectProp = (OwlObjectProperty) property;
               Iterator<ElementKey> it4 = objectProp.getRange().keySet().iterator();
               while (it4.hasNext()) {
                  ElementKey propKey = it4.next();
                  if (owlClasses.containsKey(propKey)) {
                     GraphMLNode rangeNode = elementToNode.get(propKey);
                     GraphMLEdge edge = graph.addEdge(rangeNode, theNode);
                     EdgeLabel label = edge.createLabel(true);
                     label.setLabel(property.getName());
                     Arrows arrows = edge.getArrows();
                     arrows.setSource(Arrows.STANDARD);
                     arrows.setTarget(Arrows.NONE);
                     if (showRelationsConstraints) {
                        if (property.hasCardinalityRestriction()) {
                           addCardinalityRestriction(property, edge);
                        } else {
                           addDefaultCardinalityRestriction(edge);
                        }
                     }
                  }
               }
            } else {
               OwlDatatypeProperty dataProperty = (OwlDatatypeProperty) property;
               GraphMLNode propertyNode = addDataProperty(theClass, dataProperty);

               GraphMLEdge edge = graph.addEdge(propertyNode, theNode);
               EdgeLabel elabel = edge.createLabel(true);
               elabel.setLabel(property.getName());
               Arrows arrows = edge.getArrows();
               arrows.setSource(Arrows.STANDARD);
               arrows.setTarget(Arrows.NONE);
            }
         }
      }
      factory.saveDiagram(graph, file);
   }

   private String getType(OwlDatatypeProperty dataProperty) {
      Map<ElementKey, OwlDatatype> types = dataProperty.getTypes();
      if (types != null && types.size() == 1) {
         OwlDatatype dataType = types.values().iterator().next();
         String ns = dataType.getNamespace();
         if (ns != null && ns.equals(DEFAULT_NS)) {
            String name = "xs: " + dataType.getName();
            return name;
         }
         return null;
      } else {
         return null;
      }
   }

   private void addDefaultCardinalityRestriction(GraphMLEdge edge) {
      EdgeLabel label = edge.createAdditionalLabel("0..n", 0.02f);
      label.setAutoFlip(false);
      label.setAutoRotate(false);
   }

   private void addCardinalityRestriction(OwlProperty property, GraphMLEdge edge) {
      StringBuilder buf = new StringBuilder();
      boolean isUniqueCardinality = false;
      if (property.hasMinCardinality()) {
         int minCardinality = property.getMinCardinality();
         int maxCardinality = property.getMaxCardinality();
         if (minCardinality == maxCardinality) {
            isUniqueCardinality = true;
            buf.append(minCardinality);
         } else {
            buf.append(minCardinality);
         }
      } else {
         buf.append(0);
      }
      if (!isUniqueCardinality) {
         buf.append("...");
         if (property.hasMaxCardinality()) {
            int maxCardinality = property.getMaxCardinality();
            buf.append(maxCardinality);
         } else {
            buf.append("n");
         }
      }
      EdgeLabel label = edge.createAdditionalLabel(buf.toString(), 0.02f);
      label.setAutoFlip(false);
      label.setAutoRotate(false);
   }

   @Override
   public String getMessage() {
      return this.getLongDescription() + " exported successfully";
   }
}
