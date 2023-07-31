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
package org.girod.ontobrowser.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import org.girod.ontobrowser.model.ElementKey;
import org.girod.ontobrowser.model.NamedOwlElement;
import org.girod.ontobrowser.model.OwlClass;
import org.girod.ontobrowser.model.OwlDatatypeProperty;
import org.girod.ontobrowser.model.OwlIndividual;
import org.girod.ontobrowser.model.OwlObjectProperty;
import org.girod.ontobrowser.model.OwlProperty;
import org.girod.ontobrowser.model.restriction.OwlRestriction;
import org.mdi.bootstrap.swing.MDIDialog;
import org.mdiutil.swing.GenericDialog;

/**
 * This Dialog shows the dependencies of an element.
 *
 * @since 0.5
 */
public class ShowDependenciesDialog extends GenericDialog implements MDIDialog {
   protected final NamedOwlElement element;
   protected final GraphPanel panel;
   protected JList<Object> list;
   protected DefaultListModel<Object> model = new DefaultListModel<>();

   public ShowDependenciesDialog(NamedOwlElement element, GraphPanel panel, Component parent) {
      super("Dependencies of " + element.toString());
      this.element = element;
      this.panel = panel;
      this.setResizable(true);
   }

   protected void createList() {
      list = new DependenciesList(model);

      list.addMouseListener(new MouseAdapter() {
         public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON3) {
               rightClickOnList(e.getX(), e.getY());
            }
         }
      });
   }

   @Override
   protected void createPanel() {
      Container pane = dialog.getContentPane();
      pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
      pane.add(Box.createVerticalStrut(5));
      initializeList();

      // create the list panel
      JPanel listPanel = new JPanel();
      listPanel.setLayout(new BorderLayout());
      listPanel.add(new JScrollPane(list), BorderLayout.CENTER);
      pane.add(listPanel);

      JPanel okpanel = this.createOKPanel();
      pane.add(okpanel);
   }

   protected void initializeList() {
      if (element instanceof OwlIndividual) {
         // Classes of the Individual
         model.addElement("Parent Classes");
         OwlIndividual individual = (OwlIndividual) element;
         SortedMap<ElementKey, OwlClass> map = new TreeMap<>(individual.getParentClasses());
         Iterator<Entry<ElementKey, OwlClass>> it = map.entrySet().iterator();
         while (it.hasNext()) {
            Entry<ElementKey, OwlClass> entry = it.next();
            OwlClass theClass = entry.getValue();
            model.addElement(theClass);
         }
      } else if (element instanceof OwlClass) {
         OwlClass theClass = (OwlClass) element;
         // data properties of the Class
         model.addElement("Data Properties");
         SortedMap<ElementKey, OwlProperty> mapp = new TreeMap<>(theClass.getOwlProperties());
         Iterator<Entry<ElementKey, OwlProperty>> itp = mapp.entrySet().iterator();
         while (itp.hasNext()) {
            Entry<ElementKey, OwlProperty> entry = itp.next();
            OwlProperty property = entry.getValue();
            if (property instanceof OwlDatatypeProperty) {
               model.addElement(new PropertyBridge(entry.getValue(), true));
            }
         }
         // domain properties of the Class
         model.addElement("Object Properties Domain");
         mapp = new TreeMap<>(theClass.getOwlProperties());
         itp = mapp.entrySet().iterator();
         while (itp.hasNext()) {
            Entry<ElementKey, OwlProperty> entry = itp.next();
            OwlProperty property = entry.getValue();
            if (property instanceof OwlObjectProperty) {
               model.addElement(new PropertyBridge(entry.getValue(), false));
            }
         }
         // range properties of the Class
         model.addElement("Object Properties Range");
         mapp = new TreeMap<>(theClass.getRangeOwlProperties());
         itp = mapp.entrySet().iterator();
         while (itp.hasNext()) {
            Entry<ElementKey, OwlProperty> entry = itp.next();
            OwlProperty property = entry.getValue();
            if (property instanceof OwlObjectProperty) {
               model.addElement(new PropertyBridge(entry.getValue(), true));
            }
         }
         // individuals of the Class
         model.addElement("Individuals");
         SortedMap<ElementKey, OwlIndividual> mapi = new TreeMap<>(theClass.getIndividuals());
         Iterator<Entry<ElementKey, OwlIndividual>> iti = mapi.entrySet().iterator();
         while (iti.hasNext()) {
            Entry<ElementKey, OwlIndividual> entry = iti.next();
            OwlIndividual individual = entry.getValue();
            model.addElement(individual);
         }
      } else if (element instanceof OwlProperty) {
         OwlProperty theProperty = (OwlProperty) element;

         // Classes of the domain
         model.addElement("Domain Classes");
         SortedMap<ElementKey, OwlRestriction> map = new TreeMap<>(theProperty.getDomain());
         Iterator<Entry<ElementKey, OwlRestriction>> it = map.entrySet().iterator();
         while (it.hasNext()) {
            Entry<ElementKey, OwlRestriction> entry = it.next();
            OwlRestriction restriction = entry.getValue();
            model.addElement(restriction.getOwlClass());
         }
         if (theProperty instanceof OwlObjectProperty) {
            OwlObjectProperty theObjectProperty = (OwlObjectProperty) element;
            // Classes of the range
            model.addElement("Range Classes");
            map = new TreeMap<>(theObjectProperty.getRange());
            it = map.entrySet().iterator();
            while (it.hasNext()) {
               Entry<ElementKey, OwlRestriction> entry = it.next();
               OwlRestriction restriction = entry.getValue();
               model.addElement(restriction.getOwlClass());
            }
         }
      }

      this.createList();
      list.setCellRenderer(new DependenciesListCellRenderer());
   }

   protected void rightClickOnList(int x, int y) {
      Object o = list.getSelectedValue();
      if (o != null) {
         OwlClass selectedClass = (OwlClass) o;
         JPopupMenu menu = new JPopupMenu();
         JMenuItem item = new JMenuItem("Goto Class");
         item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               gotoClass(selectedClass);
            }
         });
         menu.add(item);
         menu.show(list, x, y);
      }
   }

   private void gotoClass(OwlClass selectedClass) {
      ElementKey key = selectedClass.getKey();
      panel.selectClass(key);
   }

   private class DependenciesList<E> extends JList {
      public DependenciesList(ListModel<Object> dataModel) {
         super(dataModel);
      }

      @Override
      public Dimension getPreferredScrollableViewportSize() {
         return new Dimension(400, 200);
      }
   }
}