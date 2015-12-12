/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010, 2011 Albert Pham <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.skmcl.swing;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.sk89q.skmcl.LauncherException;
import com.sk89q.skmcl.application.Application;
import com.sk89q.skmcl.application.LatestSnapshot;
import com.sk89q.skmcl.application.LatestStable;
import com.sk89q.skmcl.application.Version;
import com.sk89q.skmcl.concurrent.AbstractWorker;
import com.sk89q.skmcl.concurrent.ExecutorWorkerService;
import com.sk89q.skmcl.concurrent.SwingProgressObserver;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;

import static com.sk89q.skmcl.util.SharedLocale._;

public class VersionListDialog extends JDialog {

    private final ExecutorWorkerService executor = new ExecutorWorkerService(
            MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor()));
    private final Application application;
    private JList versionsList;
    @Getter @Setter @NonNull
    private Version version;

    public VersionListDialog(Window owner, @NonNull final Application application,
                             @NonNull Version version) {
        super(owner, ModalityType.DOCUMENT_MODAL);

        this.application = application;
        this.version = version;

        new SwingProgressObserver(this, executor);

        setTitle(_("selectVersions.title"));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        initComponents();
        setSize(new Dimension(250, 380));
        setResizable(true);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel();
        versionsList = new JList();
        JScrollPane versionsScroll = new JScrollPane(versionsList);
        LinedBoxPanel buttonsPanel = new LinedBoxPanel(true).fullyPadded();
        JButton cancelButton = new JButton(_("button.cancel"));
        JButton selectButton = new JButton(_("button.select"));

        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(versionsScroll, BorderLayout.CENTER);

        buttonsPanel.addGlue();
        buttonsPanel.addElement(selectButton);
        buttonsPanel.addElement(cancelButton);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);

        cancelButton.addActionListener(ActionListeners.dispose(this));

        selectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object selected = versionsList.getSelectedValue();
                if (selected != null && selected instanceof Version) {
                    version = (Version) selected;
                }
                dispose();
            }
        });

        getRootPane().setDefaultButton(selectButton);

        versionsList.addMouseListener(
                new DoubleClickToButtonAdapter(selectButton));

        ListenableFuture<?> future = executor.submit(new AbstractWorker<Object>() {
            @Override
            protected void run() throws Exception {
                try {
                    setVersions(application.getAvailable());
                } catch (IOException e) {
                    dispose();
                    throw new LauncherException(e,
                            _("versionList.failedFetchError"));
                } catch (InterruptedException e) {
                    dispose();
                }
            }

            @Override
            public String getLocalizedTitle() {
                return _("selectVersions.fetchingVersionsTitle");
            }

            @Override
            public boolean shouldConfirmInterrupt() {
                return false;
            }
        });

        SwingHelper.addErrorDialogCallback(future, this);
    }

    private void setVersions(final List<? extends Version> versions) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                DefaultListModel model = new DefaultListModel();
                model.addElement(new LatestStable());
                if (application.hasSnapshots()) {
                    model.addElement(new LatestSnapshot());
                }
                for (Version version : versions) {
                    model.addElement(version);
                }
                versionsList.setModel(model);
                versionsList.setSelectedValue(version, true);
            }
        });
    }

}
