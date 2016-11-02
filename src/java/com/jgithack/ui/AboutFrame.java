/**
 * JHack.com Inc.
 * Copyright (c) 2004-2016 All Rights Reserved.
 */
package com.jgithack.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import javax.swing.WindowConstants;

/**
 * AboutFrame
 * 
 * @author Albert Wang
 * @version $Id: AboutFrame.java, V0.1 Nov 1, 2016 11:12:38 PM jawangwen@qq.com $
 */
public class AboutFrame extends JFrame {
    /** serial version */
    private static final long serialVersionUID = -3921661806821542826L;

    private String            message          = "<html><center><h3>GitHack v1.0</h3></center>\n"
                                                 + "<h3>JGitHack is a Java tool using \".git leak\""
                                                 + " to restore web project.</h3><br>\n"
                                                 + "Albert Wang(jawangwen@qq.com)</html>";
    private JPanel            aboutPanel;
    private JEditorPane       editorPane;
    private JScrollPane       scrollPane;
    private JButton           okButton;

    /**
     * Creates new form aboutFrame
     */
    public AboutFrame() {
        initComponents();
        editorPane.setCaretPosition(0);
    }

    private void initComponents() {
        okButton = new JButton();
        aboutPanel = new JPanel();
        scrollPane = new JScrollPane();
        editorPane = new JEditorPane();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        okButton.setText("OK");
        okButton.setAlignmentX(0.5F);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                dispose();
            }
        });

        aboutPanel.setPreferredSize(new Dimension(450, 300));

        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        editorPane.setText(message);
        editorPane.setMinimumSize(new Dimension(150, 150));
        editorPane.setPreferredSize(new Dimension(150, 150));
        scrollPane.setViewportView(editorPane);

        GroupLayout aboutPanelLayout = new GroupLayout(aboutPanel);
        aboutPanel.setLayout(aboutPanelLayout);
        aboutPanelLayout.setHorizontalGroup(aboutPanelLayout.createParallelGroup(
            GroupLayout.Alignment.LEADING).addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 444,
            Short.MAX_VALUE));
        aboutPanelLayout.setVerticalGroup(aboutPanelLayout.createParallelGroup(
            GroupLayout.Alignment.LEADING).addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 311,
            Short.MAX_VALUE));

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout
            .createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(
                layout.createSequentialGroup().addGap(168, 168, 168).addComponent(okButton)
                    .addContainerGap(227, Short.MAX_VALUE))
            .addComponent(aboutPanel, GroupLayout.DEFAULT_SIZE, 444, Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
            GroupLayout.Alignment.TRAILING,
            layout.createSequentialGroup()
                .addComponent(aboutPanel, GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(okButton)
                .addContainerGap()));
        pack();
    }
}
