package rippledown.attribute;

public class AttributeEditor {

    private static final UserStrings us = AttributeUserStrings.instance();

    public static final Color DEFAULT_HEADER_BACKGROUND = SystemColor.WHITE;
    public static final Color DEFAULT_HIGHLIGHT_BACKGROUND = SystemColor.YELLOW;
    public static final Color DEFAULT_NORMAL_BACKGROUND = SystemColor.WHITE;

    private static final Dimension TOOLBAR_BUTTON_DIMENSION = new Dimension(30, 30);
    public static final Dimension DEFAULT_MAIN_WINDOW_SIZE = new Dimension(400, 550);

    public static final String TREE_NAME = "AttributeEditor.TreeName";

    private AttributeHandler handler;

    //Map of attribute -> external name
    private FunctionState externalNames;

    //the main window
    private JDialog dialog;

    private FindDialog finder;

    private Editor<AttributeItem> editor;

    private AttributePropertiesDialog propertiesDialog;
    private JColorChooser headerChooser;
    private JColorChooser highlightChooser;
    private Color headerBackground = DEFAULT_HEADER_BACKGROUND;

    //Manages all of the actions
    private Actions actions = new Actions();

    private JDialog editSingleAttributeDialog;
    private boolean showIndex = false;
    private boolean showExternalNames = false;

    private JCheckBoxMenuItem extCheckBox;
    private JMenu setDefaultFormatMenu;

    public AttributeEditor(AttributeHandler handler) {
        this.handler = handler;
        finder = new FindDialog(null);
        defineUI();
        //TestSetup.pause();
    }

    public void show() {
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private void reload() {
        AttributeItem selectedBefore = editor.selectedItem();

        externalNames = handler.externalNames();

        //We always want at least 1 attribute, so if there are none, create one and try again.
        Collection<AttributeItem> items = handler.attributeItems();
        if (items.isEmpty()) {
            handler.put(new PrimaryAttribute(us.message(SimpleMessages.INITIAL_NAME)));
        }
        editor.reloadItems(selectedBefore);
        AttributeItem selectedItem = editor.selectedItem();
        actions.enableActionsForSelectedAttribute(selectedItem);
    }

    private JTree tree() {
        return editor.tree();
    }

    private void defineUI() {
        dialog = new JDialog(handler.frame(), us.message(SimpleMessages.ATTRIBUTE_EDITOR), true);
        dialog.setName(SimpleMessages.ATTRIBUTE_EDITOR);
        Dimension size = handler.preferences().getDimension(PreferenceKeys.ATTRIBUTE_EDITOR_SIZE);
        dialog.setSize(size != null ? size : DEFAULT_MAIN_WINDOW_SIZE);
        dialog.getContentPane().setLayout(new BorderLayout());
        propertiesDialog = new AttributePropertiesDialog(new MyAttributePropertiesDialog());
        setMenuBar();
        //The tool bar.
        JToolBar toolBar = createToolBar();
        dialog.getContentPane().add(toolBar, BorderLayout.NORTH);

        //The component  showing the attributes.
        editor = new Editor<AttributeItem>(new TreeEditorHandler());

        reload();

        dialog.getContentPane().add(editor.ui(), BorderLayout.CENTER);

        //A button to close the dialog.
        Box box = createButtonBar();
        box.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        dialog.getContentPane().add(box, BorderLayout.SOUTH);

        //Mouse listener to listen for popup triggers & double click
    }

    private Box createButtonBar() {
        JButton closeButton = new JButton(actions.close);
        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(closeButton);
        box.add(Box.createHorizontalStrut(5));
        box.add(createHelpButton(Help.Topic.Attribute));
        return box;
    }

    private void setMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        dialog.setJMenuBar(menuBar);

        //File menu.
        JMenu fileMenu = createMenu(us, SimpleMessages.FILE);
        fileMenu.add(actions.find);
        fileMenu.add(actions.findNext);
        fileMenu.addSeparator();
        fileMenu.add(actions.exportAttributes);
        fileMenu.add(actions.importAttributes);
        fileMenu.addSeparator();
        fileMenu.add(actions.close);
        menuBar.add(fileMenu);

        //Edit menu.
        JMenu editMenu = createMenu(us, SimpleMessages.EDIT);
        menuBar.add(editMenu);
        editMenu.add(actions.addPrimary);
        editMenu.add(actions.addNRA);
        editMenu.add(actions.addISRA);
        editMenu.add(actions.addPanic);
        editMenu.add(actions.addCVA);
        editMenu.add(actions.addTEA);
        editMenu.add(actions.addRA);
        editMenu.add(actions.addMA);
        editMenu.add(actions.addGA);
        editMenu.add(actions.addEA);
        editMenu.add(actions.addPlugin);
        if (handler.interfaceType().equals(LISType.GENERIC_XML_DIRECTORY)) {
            editMenu.add(actions.addTCA);
        }
        editMenu.addSeparator();
        editMenu.add(actions.addFolder);
        editMenu.addSeparator();
        editMenu.add(actions.edit);
        editMenu.add(actions.rename);

        //View menu
        JMenu viewMenu = createMenu(us, SimpleMessages.VIEW);
        menuBar.add(viewMenu);
        viewMenu.add(actions.caseDisplay);
        viewMenu.addSeparator();
        viewMenu.add(actions.moveUp);
        viewMenu.add(actions.moveDown);
        viewMenu.add(actions.moveToTop);
        viewMenu.add(actions.moveToBottom);
        viewMenu.add(actions.moveToPosition);
        viewMenu.addSeparator();
        viewMenu.add(actions.hideAllFollowing);
        viewMenu.add(actions.showAllFollowing);

        //Options menu.
        JMenu optionsMenu = createMenu(us, SimpleMessages.OPTIONS);
        menuBar.add(optionsMenu);
        extCheckBox = new JCheckBoxMenuItem(actions.externalNamesDisplay);
        extCheckBox.setState(showExternalNames);
        optionsMenu.add(extCheckBox);
        optionsMenu.add(actions.setExternalName);
        optionsMenu.addSeparator();
        optionsMenu.add(actions.headerColor);
        optionsMenu.add(actions.highlightColor);
        optionsMenu.addSeparator();
        Formats.Predefined predefined = handler.getDefaultPredefined();
        setDefaultFormatMenu = createMenu(us, SimpleMessages.FORMAT_SET_DEFAULT_FORMAT);
        for (final FormatOption option : formatOptions(false)) {
            final JCheckBoxMenuItem optionItem = new JCheckBoxMenuItem(option.toString());
            optionItem.setName(option.keyForManager);

            //tick the menu item if it is the current default format
            optionItem.setState(Formats.Predefined.valueOf(option.keyForManager) == predefined);

            optionItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Formats.Predefined format = Formats.Predefined.valueOf(option.keyForManager);
                    handler.setDefaultFormat(format);
                    deselectAllOptionItems();
                    optionItem.setState(true);
                }

            });
            setDefaultFormatMenu.add(optionItem);
        }
        optionsMenu.add(setDefaultFormatMenu);
    }

    private void deselectAllOptionItems() {
        for (int i = 0; i < setDefaultFormatMenu.getMenuComponentCount(); i++) {
            JCheckBoxMenuItem item = (JCheckBoxMenuItem) setDefaultFormatMenu.getMenuComponent(i);
            item.setState(false);
        }
    }

    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton addNRAButton = new ToolbarButton(actions.addNRA);
        addNRAButton.setText("");
        toolBar.add(addNRAButton);
        //Add a focus listener so that the when the dialog initially shows, the focus goes to the tree().
        addNRAButton.addFocusListener(new FocusAdapter() {
            private boolean firstUse = true;

            public void focusGained(FocusEvent e) {
                if (firstUse) {
                    tree().requestFocusInWindow();
                    firstUse = false;
                }
            }
        });
        toolBar.add(new ToolbarButton(actions.addISRA));
        toolBar.add(new ToolbarButton(actions.addPanic));
        toolBar.add(new ToolbarButton(actions.addCVA));
        toolBar.add(new ToolbarButton(actions.addTEA));
        toolBar.add(new ToolbarButton(actions.addRA));
        toolBar.add(new ToolbarButton(actions.addMA));
        toolBar.addSeparator();
        toolBar.add(new ToolbarButton(actions.addFolder));

        //Buttons for moving attributes.
        toolBar.add(Box.createHorizontalStrut(10));

        toolBar.add(new ToolbarButton(actions.moveUp));
        toolBar.add(new ToolbarButton(actions.moveDown));

        toolBar.add(Box.createHorizontalStrut(10));
        toolBar.add(new ToolbarButton(actions.find));

        return toolBar;
    }

    private AttributeItem selectedItem() {
        return editor.selectedItem();
    }

    private
    @NotNull
    Color initialHighlightColour() {
        Color initialColor = handler.preferences().getColor(PreferenceKeys.HIGHLIGHT_BACKGROUND);
        if (initialColor == null) {
            initialColor = DEFAULT_HIGHLIGHT_BACKGROUND;
        }
        return initialColor;
    }

    private class EditingHandler extends DefaultEditingHandler {

        protected EditingHandler(KBHandlerBase handler) {
            super(handler);
        }

        public SequenceState attributes() {
            SequenceImpl attributeState = new SequenceImpl();
            for (AttributeItem attribute : handler.attributeItems()) {
                if (attribute.isFolder()) continue;
                attributeState.add(attribute);
            }
            return attributeState;
        }

        public JDialog editingDialog() {
            return editSingleAttributeDialog;
        }

        public RDRCase currentCase() {
            return handler.currentCase();
        }

        public Collection<String> updateFileNames() {
            return (Collection<String>) handler.invokeCommand(new AttributeUpdateFileNames());
        }

        public InSetRangeAttribute updateFromFile(InSetRangeAttribute isra) {
            return (InSetRangeAttribute) handler.invokeCommand(new AttributeUpdateFromFile(isra));
        }
    }

    private class Actions {

        AbstractAction exportAttributes = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                final ExportAttributes exportAttribute = new ExportAttributes(new ExportAttributes.ExportHandler() {

                    public Result<String> exportRequested(String filePath) {
                        try {
                            java.util.List<PrimaryAttribute> primaryAttributes = new ArrayList<PrimaryAttribute>();
                            java.util.List<String> allExternalNames = new ArrayList<String>();
                            java.util.List<DerivedAttribute> derivedAttributes = new ArrayList<DerivedAttribute>();
                            fillLists(primaryAttributes, allExternalNames, derivedAttributes);
                            ImportExportAttributes exporter = new ImportExportAttributes();
                            exporter.doExport(new File(filePath), primaryAttributes, allExternalNames, derivedAttributes);
                            return Result.createSuccess(filePath);
                        } catch (Exception exception) {
                            return Result.createCustom(us.message(SimpleMessages.EXPORT_FAILED_MSG), exception, false);
                        }
                    }
                });
                JDialog jDialog = exportAttribute.dialog(handler.frame());
                jDialog.setVisible(true);
            }

            private void fillLists(List<PrimaryAttribute> primaryAttributes, List<String> allExternalNames, List<DerivedAttribute> derivedAttributes) {
                Collection<AttributeItem> attributeItems = handler.attributeItems();
                TreeSet<AttributeItem> ordered = new TreeSet<AttributeItem>(Editor.itemComparator());
                ordered.addAll(attributeItems);
                FunctionState state = handler.externalNames();
                for (AttributeItem attributeItem : ordered) {
                    if (attributeItem.isFolder()) continue;
                    if (attributeItem.isPrimary()) {
                        Attribute attribute = (Attribute) attributeItem;
                        String externalName = (String) state.apply(attribute);
                        primaryAttributes.add((PrimaryAttribute) attribute);
                        allExternalNames.add(externalName);
                    } else {
                        //LW-769
                        Attribute attribute = (Attribute) attributeItem;
                        if (attribute instanceof CalculatedValueAttribute || attribute instanceof EpisodeAttribute) {
                            derivedAttributes.add((DerivedAttribute) attribute);
                        }
                    }
                }
            }
        };

        AbstractAction importAttributes = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String confirmationQuestion = handler.importFromExcelConfirmationQuestion();
                if (confirmationQuestion != null) {
                    String title = us.message(SimpleMessages.IMPORT_ATTRIBUTES_WARNING_TITLE);
                    if (JOptionPane.showConfirmDialog(dialog, confirmationQuestion, title, JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                        return;
                    }
                }
                ImportAttributes importAttributes = new ImportAttributes(new ImportAttributes.ImportHandler() {
                    public Result<String> importRequested(String filePath) {
                        try {
                            ImportExportAttributes exporter = new ImportExportAttributes();
                            String[][] arrays = exporter.doImport(new File(filePath));
                            Result<List<AttributeLite>> result = AttributeLite.factory(arrays);
                            if (!result.pass()) {
                                return Result.createCustom(us.message(SimpleMessages.IMPORT_FAILED_MSG), result.exception(), false);
                            }
                            handler.importAttributes(result.value());
                            return Result.createSuccess(us.message(SimpleMessages.IMPORT_SUCCESSFUL_MSG));
                        } catch (Exception exception) {
                            return Result.createCustom(us.message(SimpleMessages.IMPORT_FAILED_MSG), exception, false);
                        }
                    }
                });
                final JDialog importAttributesDialog = importAttributes.dialog(dialog);
                importAttributesDialog.setVisible(true);
                //Reload AFTER the modal dialog has been disposed. You can't reload while the modal dialog is showing as the focusing gets into an inconsistent state.
                reload();
            }
        };

        AbstractAction find = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                finder.find(tree());
            }
        };

        AbstractAction findNext = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                finder.findNext(tree());
            }
        };

        AbstractAction close = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                handler.preferences().putDimension(PreferenceKeys.ATTRIBUTE_EDITOR_SIZE, dialog.getSize());
                dialog.dispose();
            }
        };

        AbstractAction externalNamesDisplay = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                showExternalNames = extCheckBox.getState();
                tree().repaint();
            }
        };

        AbstractAction edit = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                Attribute attribute = (Attribute) selectedItem();
                String title = us.formattedMessage(OnePlaceMessages.EDIT, attribute.name());
                editSingleAttributeDialog = new JDialog(dialog, title, true);
                attribute.displayForEditing(new EditingHandler(handler));
                editSingleAttributeDialog.setLocationRelativeTo(null); //show in centre of screen
                editSingleAttributeDialog.setVisible(true);
            }
        };

        AbstractAction rename = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                propertiesDialog = new AttributePropertiesDialog(new MyAttributePropertiesDialog());
                propertiesDialog.showDialog();
                propertiesDialog.refresh(selectedItem());
            }
        };

        AbstractAction addPrimary = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String title = us.buttonLabel(SimpleMessages.ADD_PRIMARY);
                showAddAttributeItemDialog(title, new PrimaryAttribute(), null);
            }
        };

        AbstractAction addNRA = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String titleStart = us.buttonLabel(SimpleMessages.ADD_NRA);
                String title = us.formattedMessage(TwoPlaceMessages.ADD_ATTRIBUTE_REFERRING_TO, titleStart, selectedItem().name());
                showAddAttributeItemDialog(title, new NumericRangeAttribute(), edit);
            }
        };

        AbstractAction addISRA = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String titleStart = us.buttonLabel(SimpleMessages.ADD_ISRA);
                String title = us.formattedMessage(TwoPlaceMessages.ADD_ATTRIBUTE_REFERRING_TO, titleStart, selectedItem().name());
                showAddAttributeItemDialog(title, new InSetRangeAttribute(), edit);
            }
        };

        AbstractAction addPanic = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String titleStart = us.buttonLabel(SimpleMessages.ADD_PANIC);
                String title = us.formattedMessage(TwoPlaceMessages.ADD_ATTRIBUTE_REFERRING_TO, titleStart, selectedItem().name());
                showAddAttributeItemDialog(title, new PanicDeltaAttribute(), edit);
            }
        };

        AbstractAction addCVA = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String title = us.buttonLabel(SimpleMessages.ADD_CVA);
                showAddAttributeItemDialog(title, new CalculatedValueAttribute(), edit);
            }
        };

        AbstractAction addTEA = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String title = us.buttonLabel(SimpleMessages.ADD_TEA);
                showAddAttributeItemDialog(title, new TextExtractionAttribute(), edit);
            }
        };

        AbstractAction addRA = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String title = us.buttonLabel(SimpleMessages.ADD_RA);
                showAddAttributeItemDialog(title, new RelationAttribute(), edit);
            }
        };

        AbstractAction addMA = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String title = us.buttonLabel(SimpleMessages.ADD_MA);
                showAddAttributeItemDialog(title, new MergedAttribute(), edit);
            }
        };

        AbstractAction addGA = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String title = us.buttonLabel(SimpleMessages.ADD_GA);
                showAddAttributeItemDialog(title, new GroupAttribute(), edit);
            }
        };

        AbstractAction addEA = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String title = us.buttonLabel(SimpleMessages.ADD_EA);
                showAddAttributeItemDialog(title, new EpisodeAttribute(), edit);
            }
        };

        AbstractAction addTCA = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String title = us.buttonLabel(SimpleMessages.ADD_TCA);
                showAddAttributeItemDialog(title, new TextCondenserAttribute(), edit);
            }
        };

        AbstractAction addPlugin = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String title = us.buttonLabel(SimpleMessages.ADD_PLUGIN);
                showAddAttributeItemDialog(title, new PluginAttribute(), edit);
            }
        };

        AbstractAction addFolder = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String title = us.buttonLabel(SimpleMessages.ADD_FOLDER);
                showAddAttributeItemDialog(title, new AttributeFolder(), null);
            }
        };

        AbstractAction moveDown = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                editor.moveDown();
                reload();
            }
        };

        AbstractAction moveUp = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                editor.moveUp();
                reload();
            }
        };

        AbstractAction moveToBottom = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                AttributeItem attribute = selectedItem();
                handler.move(null, attribute, editor.numberOfSiblings(attribute));
                reload();
            }
        };

        AbstractAction moveToTop = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                AttributeItem attribute = selectedItem();
                handler.move(null, attribute, 0);
                reload();
            }
        };

        AbstractAction moveToPosition = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                propertiesDialog = new AttributePropertiesDialog(new MyAttributePropertiesDialog());
                propertiesDialog.showDialog();
                propertiesDialog.refresh(selectedItem());
                propertiesDialog.putFocusOnPositionField();
            }
        };

        AbstractAction caseDisplay = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                propertiesDialog = new AttributePropertiesDialog(new MyAttributePropertiesDialog());
                propertiesDialog.showDialog();
                propertiesDialog.refresh(selectedItem());
            }
        };

        AbstractAction setExternalName = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                new SetExternalNameDialog((PrimaryAttribute) selectedItem()).promptForNameThenSet();
            }
        };

        private ActionListener headerColorSelected = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                handler.preferences().putColor(PreferenceKeys.HEADER_BACKGROUND, headerChooser.getColor());
                headerBackground = headerChooser.getColor();
                tree().repaint();
            }
        };

        AbstractAction headerColor = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                Color initialColor = handler.preferences().getColor(PreferenceKeys.HEADER_BACKGROUND);
                if (initialColor == null) {
                    initialColor = DEFAULT_HEADER_BACKGROUND;
                }
                headerChooser = new JColorChooser(initialColor);
                JColorChooser.createDialog(dialog, us.message(SimpleMessages.CHOOSE_HEADER_COLOR), true, headerChooser, headerColorSelected, null).setVisible(true);
            }
        };

        private ActionListener highlightColorSelected = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                handler.preferences().putColor(PreferenceKeys.HIGHLIGHT_BACKGROUND, highlightChooser.getColor());
                tree().repaint();
            }
        };

        AbstractAction highlightColor = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                Color initialColor = initialHighlightColour();
                highlightChooser = new JColorChooser(initialColor);
                JColorChooser.createDialog(dialog, us.message(SimpleMessages.CHOOSE_HIGHLIGHT_COLOR), true, highlightChooser, highlightColorSelected, null).setVisible(true);
            }
        };


        AbstractAction hideAllFollowing = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                handler.hideAllFollowing(editor.selectedItem());
                reload();
            }
        };

        AbstractAction showAllFollowing = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                handler.showAllFollowing(editor.selectedItem());
                reload();
            }
        };

        private Actions() {
            setupAction(find, us, SimpleMessages.FIND);
            setupAction(findNext, us, SimpleMessages.FIND_NEXT);
            setupAction(exportAttributes, us, SimpleMessages.EXPORT_ATTRIBUTES);
            setupAction(importAttributes, us, SimpleMessages.IMPORT_PRIMARY_ATTRIBUTES);
            setupAction(close, us, SimpleMessages.CLOSE);
            setupAction(addPrimary, us, SimpleMessages.ADD_PRIMARY);
            setupAction(addNRA, us, SimpleMessages.ADD_NRA);
            setupAction(addISRA, us, SimpleMessages.ADD_ISRA);
            setupAction(addPanic, us, SimpleMessages.ADD_PANIC);
            setupAction(addCVA, us, SimpleMessages.ADD_CVA);
            setupAction(addTEA, us, SimpleMessages.ADD_TEA);
            setupAction(addRA, us, SimpleMessages.ADD_RA);
            setupAction(addMA, us, SimpleMessages.ADD_MA);
            setupAction(addGA, us, SimpleMessages.ADD_GA);
            setupAction(addEA, us, SimpleMessages.ADD_EA);
            setupAction(addPlugin, us, SimpleMessages.ADD_PLUGIN);
            setupAction(addFolder, us, SimpleMessages.ADD_FOLDER);
            setupAction(addTCA, us, SimpleMessages.ADD_TCA);
            setupAction(edit, us, SimpleMessages.EDIT);
            setupAction(rename, us, SimpleMessages.RENAME);
            setupAction(moveDown, us, SimpleMessages.MOVE_DOWN);
            setupAction(moveUp, us, SimpleMessages.MOVE_UP);
            setupAction(moveToTop, us, SimpleMessages.MOVE_TO_TOP);
            setupAction(moveToBottom, us, SimpleMessages.MOVE_TO_BOTTOM);
            setupAction(moveToPosition, us, SimpleMessages.MOVE_TO_POSITION);
            setupAction(caseDisplay, us, SimpleMessages.CASE_DISPLAY);
            setupAction(headerColor, us, SimpleMessages.HEADER_COLOR);
            setupAction(highlightColor, us, SimpleMessages.HIGHLIGHT_COLOR);
            setupAction(externalNamesDisplay, us, SimpleMessages.EXTERNAL_NAMES_DISPLAY);
            setupAction(setExternalName, us, SimpleMessages.SET_EXTERNAL_NAME);
            setupAction(hideAllFollowing, us, SimpleMessages.HIDE_ALL_FOLLOWING);
            setupAction(showAllFollowing, us, SimpleMessages.SHOW_ALL_FOLLOWING);
        }

        private void enableActionsForSelectedAttribute(@Nullable AttributeItem attributeItem) {
            find.setEnabled(true);
            findNext.setEnabled(true);
            close.setEnabled(true);

            boolean atTop = selectedAttributeIsFirstSibling();
            moveUp.setEnabled(!atTop);
            moveToTop.setEnabled(!atTop);

            boolean atBottom = selectedAttributeIsLastSibling();
            moveDown.setEnabled(!atBottom);
            moveToBottom.setEnabled(!atBottom);
            hideAllFollowing.setEnabled(!atBottom);
            showAllFollowing.setEnabled(!atBottom);

            //only allow the user to create a derived attribute if a primary is selected
            boolean isPrimary = attributeItem != null && attributeItem.isPrimary();
            addCVA.setEnabled(attributeItem != null && !attributeItem.isFolder());
            addMA.setEnabled(attributeItem != null && !attributeItem.isFolder());
            addGA.setEnabled(attributeItem != null && !attributeItem.isFolder());
            addFolder.setEnabled(true);
            addEA.setEnabled(attributeItem != null && !attributeItem.isFolder());
            addNRA.setEnabled(isPrimary);
            addISRA.setEnabled(isPrimary);
            addPanic.setEnabled(isPrimary);
            addTEA.setEnabled(isPrimary);
            addRA.setEnabled(isPrimary);

            //only allow 'edit' for derived attributes, i.e., those that can't be referred to
            edit.setEnabled(attributeItem != null && !isPrimary && !attributeItem.isFolder());
            rename.setEnabled(attributeItem != null);

            //only allow the user to set the external name if a primary is selected
            setExternalName.setEnabled(isPrimary);
        }

        private boolean selectedAttributeIsFirstSibling() {
            return editor.selectedItemIsFirstSibling();
        }

        private boolean selectedAttributeIsLastSibling() {
            return editor.selectedItemIsLastSibling();
        }
    }

    public static Icon iconForAttribute(Object attribute) {
        if (!(attribute instanceof Attribute)) {
            return null;
        }
        return AttributeIconFactory.iconForAttribute((Attribute) attribute);
    }

    private boolean isHidden(AttributeItem value) {
        if (value.isFolder()) return false;
        Attribute attribute = (Attribute) value;
        return Attribute.DisplayType.HIDDEN.equals(attribute.displayType()) && !(attribute instanceof ConclusionAttribute);
    }

    private void repositionAttribute(AttributeItem selected, int newPosition_base1) {
        int oldPosition_base1 = editor.itemPosition(selected) + 1;
        //Is the new Position sensible? If not, do nothing.
        if (newPosition_base1 == oldPosition_base1) return;
        if (newPosition_base1 < 1) return;
        if (newPosition_base1 > editor.numberOfSiblings(selected)) return;

        //The moveToIndex_base0 is the same as the base 1 position if moving down, but one less if moving up.
        /*
               newPosition_base1   moveToIndex_base0
                                   0
           A           1
                                   1
           B           2
                                   2
           C           3
                                   3
           D           4
                                   4
        */
        int moveToIndex_base0 = newPosition_base1;

        if (newPosition_base1 < oldPosition_base1) {
            --moveToIndex_base0;
        }
        handler.move(null, selected, moveToIndex_base0);
    }

    private void showAddAttributeItemDialog(String title, AttributeItemFactory factory, AbstractAction actionAfterCreate) {
        NewAttributeDialog nad = new NewAttributeDialog(new NewAttributeDialogHandler(title, factory));
        nad.showDialog();
        if (nad.attributeItem() != null) {
            //An attribute was created, reload the list and select it
            editor.reloadItems(nad.attributeItem());
            if (actionAfterCreate != null) actionAfterCreate.actionPerformed(null);
        }
    }

    private class SetExternalNameDialog {
        private OkCancelOrCloseDialog setExtNameDialog;
        private PrimaryAttribute attribute;

        SetExternalNameDialog(PrimaryAttribute attribute) {
            this.attribute = attribute;
        }

        private void promptForNameThenSet() {
            JLabel nameLabel = new JLabel(us.buttonLabel(SimpleMessages.SET_EXTERNAL_NAME));
            nameLabel.setDisplayedMnemonic(us.mnemonic(SimpleMessages.SET_EXTERNAL_NAME));
            String initialValue = (String) externalNames.apply(attribute);
            final JTextField nameField = new JTextField(15);
            nameField.setText(initialValue);
            nameField.selectAll();
            nameField.setMaximumSize(new Dimension(270, 30));
            nameLabel.setLabelFor(nameField);
            Box box = Box.createHorizontalBox();
            box.add(Box.createHorizontalStrut(5));
            box.add(nameLabel);
            box.add(Box.createHorizontalStrut(10));
            box.add(nameField);
            box.add(Box.createHorizontalGlue());
            Action setExtNameAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    String proposedName = nameField.getText().trim();
                    if (!proposedName.equals("") && proposedNameIsConfirmed(proposedName)) {
                        Result result = handler.setExternalName(attribute, proposedName);
                        if (result.pass()) {
                            setExtNameDialog.dispose();
                            reload();
                        } else {
                            JOptionPane.showMessageDialog(setExtNameDialog, result.exception().getMessage());
                            nameField.requestFocusInWindow();
                            nameField.selectAll();
                        }
                    } else {
                        nameField.requestFocusInWindow();
                    }
                }
            };
            String title = us.formattedMessage(OnePlaceMessages.SET_EXTERNAL_NAME_FOR, attribute.name());
            setExtNameDialog = new OkCancelOrCloseDialog(dialog, title, new Dimension(350, 90), box, setExtNameAction);
            setExtNameDialog.setResizable(false);
            setExtNameDialog.setVisible(true);
        }

        private boolean proposedNameIsConfirmed(String proposedName) {
            String confirmTitle = us.formattedMessage(OnePlaceMessages.SET_EXTERNAL_NAME_CONFIRMATION, attribute.name());
            String msg = us.formattedMessage(TwoPlaceMessages.SET_EXTERNAL_NAME, attribute.name(), proposedName);
            return JOptionPane.showConfirmDialog(handler.frame(), msg, confirmTitle, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
        }
    }

    private class NewAttributeDialogHandler implements rippledown.attribute.gui.NewAttributeDialog.Handler {
        private final String dialogTitle;
        private final AttributeItemFactory factory;

        private NewAttributeDialogHandler(@NotNull String dialogTitle, @NotNull AttributeItemFactory factory) {
            this.dialogTitle = dialogTitle;
            this.factory = factory;
        }


        @NotNull
        @Override
        @SuppressWarnings({"unchecked"})
        public Result<AttributeItem> createAttributeItem(@NotNull String newItem) {
            return handler.addItemAfter(factory.factory(newItem, selectedItem()), selectedItem());
        }

        @NotNull
        @Override
        public Window parentWindow() {
            return dialog;
        }

        @NotNull
        @Override
        public String dialogTitle() {
            return dialogTitle;
        }
    }

    private class FormatOption {
        private String keyForManager;
        private String displayText;

        private FormatOption(String keyForManager, String displayText) {
            this.keyForManager = keyForManager;
            this.displayText = displayText;
        }

        public String toString() {
            return displayText;
        }

        /**
         * We implement equals to make it easy to set the value in the formats combo
         * box to be the same as the option in the selected attribute.
         */
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FormatOption that = (FormatOption) o;

            return keyForManager.equals(that.keyForManager);

        }

        public int hashCode() {
            return keyForManager.hashCode();
        }
    }

    private LinkedList<FormatOption> formatOptions(boolean includeDefault) {
        LinkedList<FormatOption> result = new LinkedList<FormatOption>();
        if (includeDefault) {
            result.add(new FormatOption(DefaultSampleFormat.STORAGE_STRING, us.buttonLabel(SimpleMessages.FORMAT_DEFAULT)));
        }
        result.add(new FormatOption(Formats.Predefined.SIG_FIG_1.toString(), us.buttonLabel(SimpleMessages.FORMAT_SF1)));
        result.add(new FormatOption(Formats.Predefined.SIG_FIG_2.toString(), us.buttonLabel(SimpleMessages.FORMAT_SF2)));
        result.add(new FormatOption(Formats.Predefined.SIG_FIG_3.toString(), us.buttonLabel(SimpleMessages.FORMAT_SF3)));
        result.add(new FormatOption(Formats.Predefined.SIG_FIG_4.toString(), us.buttonLabel(SimpleMessages.FORMAT_SF4)));
        result.add(new FormatOption(Formats.Predefined.AS_IS.toString(), us.buttonLabel(SimpleMessages.FORMAT_AS_IS)));
        return result;
    }

    private class ToolbarButton extends JButton {
        public ToolbarButton(AbstractAction action) {
            super(action);
            setText("");
        }

        @Override
        public Dimension getMaximumSize() {
            return TOOLBAR_BUTTON_DIMENSION;
        }

        @Override
        public Dimension getMinimumSize() {
            return TOOLBAR_BUTTON_DIMENSION;
        }

        @Override
        public Dimension getPreferredSize() {
            return TOOLBAR_BUTTON_DIMENSION;
        }
    }

    private class TreeEditorHandler implements Editor.Handler<AttributeItem> {

        private Font normalFont = new JLabel().getFont().deriveFont(Font.PLAIN);
        private Font hiddenFont = normalFont.deriveFont(Font.ITALIC);
        private Color hiddenForeground = Color.GRAY;
        private JTree previousTree;

        public TreeEditorHandler() {
            Color headerInPreferences = handler.preferences().getColor(PreferenceKeys.HEADER_BACKGROUND);
            if (headerInPreferences != null) {
                headerBackground = headerInPreferences;
            }
        }

        @Override
        public void applyCustomRendering(@NotNull AttributeItem item, DefaultTreeCellRenderer renderer, boolean isSelected) {
            if (editor == null) {//still being constructed
                return;
            }
            StringBuffer sb = new StringBuffer();
            if (showIndex) {
                sb.append(editor.itemPosition(item) + 1);
                sb.append("  ");
            }
            sb.append(item.name());

            if (showExternalNames && externalNames != null) {
                //Note that not all attributes have an external name - str 1736
                if (externalNames.inDomain(item)) {
                    sb.append(" (");
                    sb.append(externalNames.apply(item).toString());
                    sb.append(")");
                }
            }
            renderer.setForeground(isHidden(item) ? hiddenForeground : tree().getForeground());
            renderer.setFont(isHidden(item) ? hiddenFont : normalFont);
            renderer.setText(sb.toString());

            if (!item.isFolder()) {
                Attribute.DisplayType displayType = ((Attribute) item).displayType();
                if (Attribute.DisplayType.IN_HEADER.equals(displayType)) {
                    renderer.setBackgroundNonSelectionColor(headerBackground);
                } else if (Attribute.DisplayType.HIGHLIGHTED.equals(displayType)) {
                    renderer.setBackgroundNonSelectionColor(((Attribute) item).color());
                } else {
                    renderer.setBackgroundNonSelectionColor(DEFAULT_NORMAL_BACKGROUND);
                }
            }
        }

        @Override
        public boolean showDescriptionPane() {
            return false;
        }

        @NotNull
        @Override
        public String treeName() {
            return TREE_NAME;
        }

        @NotNull
        @Override
        public Collection<AttributeItem> items() {
            return handler.attributeItems();
        }

        @Override
        public void selectionChanged(AttributeItem item, boolean isLeaf) {
            if (editor == null)
                return;  // item selection can be triggered while the editor is being constructed.
            if (item != null) {
                if (propertiesDialog != null && propertiesDialog.isVisible()) {
                    propertiesDialog.refresh(item);
                }
            }
            actions.enableActionsForSelectedAttribute(item);
        }

        @Override
        public void treeChanged(JTree tree) {
            previousTree = tree;
        }

        @Nullable
        @Override
        public JTree previousTree() {
            return previousTree;
        }

        @Override
        public void moveItem(AttributeItem parent, @NotNull AttributeItem item, int index) {
            handler.move(parent, item, index);
        }

        @NotNull
        @Override
        public List<AbstractButton> buttons() {
            return Arrays.asList();
        }

        @NotNull
        @Override
        public List<JMenuItem> popupItems() {
            LinkedList<JMenuItem> items = new LinkedList<JMenuItem>();
            items.add(new JMenuItem(actions.addPrimary));
            items.add(new JMenuItem(actions.addNRA));
            items.add(new JMenuItem(actions.addISRA));
            items.add(new JMenuItem(actions.addPanic));
            items.add(new JMenuItem(actions.addCVA));
            items.add(new JMenuItem(actions.addTEA));
            items.add(new JMenuItem(actions.addRA));
            items.add(new JMenuItem(actions.addMA));
            items.add(new JMenuItem(actions.addGA));
            items.add(new JMenuItem(actions.addEA));
            items.add(new JMenuItem(actions.addPlugin));
            items.add(null);//separator
            items.add(new JMenuItem(actions.edit));
            items.add(new JMenuItem(actions.rename));
            items.add(null);//separator
            items.add(new JMenuItem(actions.caseDisplay));
            items.add(null);//separator
            items.add(new JMenuItem(actions.addFolder));
            items.add(new JMenuItem(actions.moveUp));
            items.add(new JMenuItem(actions.moveDown));
            items.add(new JMenuItem(actions.moveToTop));
            items.add(new JMenuItem(actions.moveToBottom));
            items.add(new JMenuItem(actions.moveToPosition));
            items.add(null);//separator
            items.add(new JMenuItem(actions.hideAllFollowing));
            items.add(new JMenuItem(actions.showAllFollowing));
            items.add(null);//separator
            items.add(new JMenuItem(actions.find));
            items.add(new JMenuItem(actions.findNext));
            return items;
        }

        @Nullable
        @Override
        public AttributeItem create(AttributeItem parent, String name) {
            return null;
        }

        @NotNull
        @Override
        public String searchText(@NotNull Editor.Item item) {
            Object externalName = externalNames.apply(item);
            if (externalName != null) {
                return item.toolTipText() + " " + externalName.toString();
            }
            return item.toolTipText();
        }

        @NotNull
        @Override
        public JLabel labelForTextPane() {
            return new JLabel(us.buttonLabel(SimpleKBMessages.DESCRIPTION));
        }

        @Nullable
        @Override
        public JLabel headingForTree() {
            return null;
        }

        @Override
        public void assignListenersAndKeyStrokesTo(@NotNull JComponent tree) {
            tree.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    //Double click handling....
                    if (e.getClickCount() == 2 && !e.isPopupTrigger()) {
                        AttributeItem attr = selectedItem();
                        //For primary attributes, show the case display editor.
                        if (attr.isFolder() || attr.isPrimary()) {
                            actions.caseDisplay.actionPerformed(null);
                        } else {
                            //For derived attributes show the editor.
                            actions.edit.actionPerformed(null);
                        }
                    }
                }
            });
        }

        @Override
        public boolean isDragAndDropEnabled() {
            return true;
        }

        @Override
        public boolean isMultipleSelectionEnabled() {
            return false;
        }
    }

    private class MyAttributePropertiesDialog implements AttributePropertiesDialog.Handler {
        @Override
        public Preferences preferences() {
            return handler.preferences();
        }

        @Override
        public void setFormat(@NotNull Attribute selected, @NotNull String keyForManager) {
            handler.setFormat(selected, keyForManager);
        }

        @Override
        public void update(@NotNull AttributeItem attributeItem) {
            handler.put(attributeItem);
        }

        @Override
        public Result setName(AttributeItem selected, String proposedNewName) {
            return handler.setName(selected, proposedNewName);
        }

        @Override
        public void reload() {
            AttributeEditor.this.reload();
        }

        @Override
        public void repositionAttribute(AttributeItem attributeItem, int newPosition_base1) {
            AttributeEditor.this.repositionAttribute(attributeItem, newPosition_base1);
        }

        @Override
        public int itemPosition(AttributeItem attributeItem) {
            return editor.itemPosition(attributeItem) + 1;
        }

        @Override
        public void upArrow() {
            editor.upArrow();
        }

        @Override
        public void downArrow() {
            editor.downArrow();
        }

        @Override
        public Window parent() {
            return dialog;
        }
    }
}