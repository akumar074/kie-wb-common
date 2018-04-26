/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.stunner.project.client.editor;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.guvnor.common.services.project.client.context.WorkspaceProjectContext;
import org.guvnor.common.services.project.client.security.ProjectController;
import org.guvnor.common.services.project.model.WorkspaceProject;
import org.guvnor.common.services.shared.metadata.model.Overview;
import org.guvnor.messageconsole.client.console.widget.button.AlertsButtonMenuItemBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.stunner.client.widgets.presenters.session.SessionPresenter;
import org.kie.workbench.common.stunner.client.widgets.presenters.session.SessionPresenterFactory;
import org.kie.workbench.common.stunner.core.client.api.SessionManager;
import org.kie.workbench.common.stunner.core.client.error.DiagramClientErrorHandler;
import org.kie.workbench.common.stunner.core.client.i18n.ClientTranslationService;
import org.kie.workbench.common.stunner.core.client.session.ClientFullSession;
import org.kie.workbench.common.stunner.core.client.session.ClientReadOnlySession;
import org.kie.workbench.common.stunner.core.client.session.ClientSessionFactory;
import org.kie.workbench.common.stunner.core.client.session.command.impl.ClearSessionCommand;
import org.kie.workbench.common.stunner.core.client.session.command.impl.ClearStatesSessionCommand;
import org.kie.workbench.common.stunner.core.client.session.command.impl.CopySelectionSessionCommand;
import org.kie.workbench.common.stunner.core.client.session.command.impl.CutSelectionSessionCommand;
import org.kie.workbench.common.stunner.core.client.session.command.impl.DeleteSelectionSessionCommand;
import org.kie.workbench.common.stunner.core.client.session.command.impl.ExportToBpmnSessionCommand;
import org.kie.workbench.common.stunner.core.client.session.command.impl.ExportToJpgSessionCommand;
import org.kie.workbench.common.stunner.core.client.session.command.impl.ExportToPdfSessionCommand;
import org.kie.workbench.common.stunner.core.client.session.command.impl.ExportToPngSessionCommand;
import org.kie.workbench.common.stunner.core.client.session.command.impl.ExportToSvgSessionCommand;
import org.kie.workbench.common.stunner.core.client.session.command.impl.PasteSelectionSessionCommand;
import org.kie.workbench.common.stunner.core.client.session.command.impl.RedoSessionCommand;
import org.kie.workbench.common.stunner.core.client.session.command.impl.SessionCommandFactory;
import org.kie.workbench.common.stunner.core.client.session.command.impl.SwitchGridSessionCommand;
import org.kie.workbench.common.stunner.core.client.session.command.impl.UndoSessionCommand;
import org.kie.workbench.common.stunner.core.client.session.command.impl.ValidateSessionCommand;
import org.kie.workbench.common.stunner.core.client.session.command.impl.VisitGraphSessionCommand;
import org.kie.workbench.common.stunner.core.client.session.impl.AbstractClientFullSession;
import org.kie.workbench.common.stunner.core.client.session.impl.AbstractClientReadOnlySession;
import org.kie.workbench.common.stunner.project.client.editor.event.OnDiagramFocusEvent;
import org.kie.workbench.common.stunner.project.client.editor.event.OnDiagramLoseFocusEvent;
import org.kie.workbench.common.stunner.project.client.screens.ProjectMessagesListener;
import org.kie.workbench.common.stunner.project.client.service.ClientProjectDiagramService;
import org.kie.workbench.common.stunner.project.diagram.ProjectDiagram;
import org.kie.workbench.common.stunner.project.diagram.ProjectMetadata;
import org.kie.workbench.common.widgets.client.menu.FileMenuBuilderImpl;
import org.kie.workbench.common.widgets.metadata.client.KieEditorWrapperView;
import org.kie.workbench.common.widgets.metadata.client.validation.AssetUpdateValidator;
import org.kie.workbench.common.widgets.metadata.client.widget.OverviewWidgetPresenter;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.uberfire.backend.vfs.ObservablePath;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.events.ChangeTitleWidgetEvent;
import org.uberfire.client.workbench.type.ClientResourceType;
import org.uberfire.client.workbench.widgets.common.ErrorPopupPresenter;
import org.uberfire.ext.editor.commons.client.file.popups.SavePopUpPresenter;
import org.uberfire.ext.editor.commons.client.history.VersionRecordManager;
import org.uberfire.ext.editor.commons.client.menu.BasicFileMenuBuilder;
import org.uberfire.mocks.EventSourceMock;
import org.uberfire.mvp.Command;
import org.uberfire.workbench.model.menu.MenuItem;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(GwtMockitoTestRunner.class)
public class AbstractProjectDiagramEditorTest {

    protected static final String TITLE = "title";

    @Mock
    protected ProjectDiagram diagram;

    @Mock
    protected AbstractProjectDiagramEditor.View view;

    @Mock
    protected PlaceManager placeManager;

    @Mock
    protected ErrorPopupPresenter errorPopupPresenter;

    @Mock
    protected EventSourceMock<ChangeTitleWidgetEvent> changeTitleNotificationEvent;

    @Mock
    protected SavePopUpPresenter savePopUpPresenter;

    @Mock
    protected ClientProjectDiagramService clientProjectDiagramService;

    @Mock
    protected SessionManager sessionManager;

    @Mock
    protected SessionPresenterFactory sessionPresenterFactory;

    @Mock
    protected EventSourceMock<OnDiagramFocusEvent> onDiagramFocusEvent;

    @Mock
    protected EventSourceMock<OnDiagramLoseFocusEvent> onDiagramLostFocusEvent;

    @Mock
    protected BasicFileMenuBuilder menuBuilder;

    @Mock
    protected VersionRecordManager versionRecordManager;

    @Spy
    @InjectMocks
    protected FileMenuBuilderImpl fileMenuBuilder;

    @Mock
    protected ProjectDiagramEditorMenuItemsBuilder projectMenuItemsBuilder;

    @Mock
    protected ProjectController projectController;

    @Mock
    protected WorkspaceProjectContext workbenchContext;

    @Mock
    protected SessionCommandFactory sessionCommandFactory;

    @Mock
    protected ProjectMessagesListener projectMessagesListener;

    protected ClientResourceType resourceType;

    @Mock
    protected DiagramClientErrorHandler diagramClientErrorHandler;

    @Mock
    protected ClientTranslationService translationService;

    @Mock
    protected AlertsButtonMenuItemBuilder alertsButtonMenuItemBuilder;

    @Mock
    protected MenuItem alertsButtonMenuItem;

    @Mock
    private ClearStatesSessionCommand clearStatesSessionCommand;

    @Mock
    protected SwitchGridSessionCommand switchGridSessionCommand;

    @Mock
    protected VisitGraphSessionCommand visitGraphSessionCommand;

    @Mock
    protected ClearSessionCommand clearSessionCommand;

    @Mock
    protected DeleteSelectionSessionCommand deleteSelectionSessionCommand;

    @Mock
    protected UndoSessionCommand undoSessionCommand;

    @Mock
    protected RedoSessionCommand redoSessionCommand;

    @Mock
    protected ValidateSessionCommand validateSessionCommand;

    @Mock
    protected ExportToPngSessionCommand exportToPngSessionCommand;

    @Mock
    protected ExportToJpgSessionCommand exportToJpgSessionCommand;

    @Mock
    protected ExportToPdfSessionCommand exportToPdfSessionCommand;

    @Mock
    protected ExportToBpmnSessionCommand exportToBpmnSessionCommand;

    @Mock
    protected CopySelectionSessionCommand copySelectionSessionCommand;

    @Mock
    protected PasteSelectionSessionCommand pasteSelectionSessionCommand;

    @Mock
    protected CutSelectionSessionCommand cutSelectionSessionCommand;

    @Mock
    protected ExportToSvgSessionCommand exportToSvgSessionCommand;

    @Mock
    protected SessionPresenter fullSessionPresenter;

    @Mock
    protected SessionPresenter readOnlySessionPresenter;

    @Mock
    protected SessionPresenter.View sessionPresenterView;

    @Mock
    protected AbstractClientFullSession clientFullSession;

    @Mock
    protected AbstractClientReadOnlySession clientReadOnlySession;

    @Mock
    protected ObservablePath filePath;

    @Mock
    protected KieEditorWrapperView kieView;

    @Mock
    protected OverviewWidgetPresenter overviewWidget;

    @Mock
    protected MenuItem clearItem;

    @Mock
    protected MenuItem visitGraphItem;

    @Mock
    protected MenuItem switchGridItem;

    @Mock
    protected MenuItem deleteSelectionItem;

    @Mock
    protected MenuItem undoItem;

    @Mock
    protected MenuItem redoItem;

    @Mock
    protected MenuItem validateItem;

    @Mock
    protected MenuItem exportsItem;

    @Mock
    protected MenuItem pasteItem;

    @Mock
    protected MenuItem copyItem;

    @Mock
    protected MenuItem cutItem;

    @Captor
    protected ArgumentCaptor<Consumer<ClientFullSession>> clientFullSessionConsumerCaptor;

    @Captor
    protected ArgumentCaptor<Consumer<ClientReadOnlySession>> clientReadOnlySessionConsumerCaptor;

    @Captor
    protected ArgumentCaptor<SessionPresenter.SessionPresenterCallback> clientSessionPresenterCallbackCaptor;

    abstract class ClientResourceTypeMock implements ClientResourceType {

    }

    protected AbstractProjectDiagramEditor<ClientResourceTypeMock> presenter;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        doReturn(clearStatesSessionCommand).when(sessionCommandFactory).newClearStatesCommand();
        doReturn(switchGridSessionCommand).when(sessionCommandFactory).newSwitchGridCommand();
        doReturn(visitGraphSessionCommand).when(sessionCommandFactory).newVisitGraphCommand();
        doReturn(clearSessionCommand).when(sessionCommandFactory).newClearCommand();
        doReturn(deleteSelectionSessionCommand).when(sessionCommandFactory).newDeleteSelectedElementsCommand();
        doReturn(undoSessionCommand).when(sessionCommandFactory).newUndoCommand();
        doReturn(redoSessionCommand).when(sessionCommandFactory).newRedoCommand();
        doReturn(validateSessionCommand).when(sessionCommandFactory).newValidateCommand();
        doReturn(exportToPngSessionCommand).when(sessionCommandFactory).newExportToPngSessionCommand();
        doReturn(exportToJpgSessionCommand).when(sessionCommandFactory).newExportToJpgSessionCommand();
        doReturn(exportToSvgSessionCommand).when(sessionCommandFactory).newExportToSvgSessionCommand();
        doReturn(exportToPdfSessionCommand).when(sessionCommandFactory).newExportToPdfSessionCommand();
        doReturn(exportToBpmnSessionCommand).when(sessionCommandFactory).newExportToBpmnSessionCommand();
        doReturn(copySelectionSessionCommand).when(sessionCommandFactory).newCopySelectionCommand();
        doReturn(pasteSelectionSessionCommand).when(sessionCommandFactory).newPasteSelectionCommand();
        doReturn(cutSelectionSessionCommand).when(sessionCommandFactory).newCutSelectionCommand();

        when(sessionPresenterFactory.newPresenterEditor()).thenReturn(fullSessionPresenter);
        when(fullSessionPresenter.getInstance()).thenReturn(clientFullSession);
        when(fullSessionPresenter.withToolbar(anyBoolean())).thenReturn(fullSessionPresenter);
        when(fullSessionPresenter.withPalette(anyBoolean())).thenReturn(fullSessionPresenter);
        when(fullSessionPresenter.displayNotifications(any(Predicate.class))).thenReturn(fullSessionPresenter);
        when(fullSessionPresenter.getView()).thenReturn(sessionPresenterView);

        when(sessionPresenterFactory.newPresenterViewer()).thenReturn(readOnlySessionPresenter);
        when(readOnlySessionPresenter.getInstance()).thenReturn(clientReadOnlySession);
        when(readOnlySessionPresenter.withToolbar(anyBoolean())).thenReturn(readOnlySessionPresenter);
        when(readOnlySessionPresenter.withPalette(anyBoolean())).thenReturn(readOnlySessionPresenter);
        when(readOnlySessionPresenter.displayNotifications(any(Predicate.class))).thenReturn(readOnlySessionPresenter);
        when(readOnlySessionPresenter.getView()).thenReturn(sessionPresenterView);

        when(projectMenuItemsBuilder.newClearItem(any(Command.class))).thenReturn(clearItem);
        when(projectMenuItemsBuilder.newVisitGraphItem(any(Command.class))).thenReturn(visitGraphItem);
        when(projectMenuItemsBuilder.newSwitchGridItem(any(Command.class))).thenReturn(switchGridItem);
        when(projectMenuItemsBuilder.newDeleteSelectionItem(any(Command.class))).thenReturn(deleteSelectionItem);
        when(projectMenuItemsBuilder.newUndoItem(any(Command.class))).thenReturn(undoItem);
        when(projectMenuItemsBuilder.newRedoItem(any(Command.class))).thenReturn(redoItem);
        when(projectMenuItemsBuilder.newValidateItem(any(Command.class))).thenReturn(validateItem);
        when(projectMenuItemsBuilder.newExportsItem(any(Command.class),
                                                    any(Command.class),
                                                    any(Command.class),
                                                    any(Command.class),
                                                    any(Command.class))).thenReturn(exportsItem);
        when(projectMenuItemsBuilder.newPasteItem(any(Command.class))).thenReturn(pasteItem);
        when(projectMenuItemsBuilder.newCopyItem(any(Command.class))).thenReturn(copyItem);
        when(projectMenuItemsBuilder.newCutItem(any(Command.class))).thenReturn(cutItem);

        when(alertsButtonMenuItemBuilder.build()).thenReturn(alertsButtonMenuItem);
        when(versionRecordManager.getPathToLatest()).thenReturn(filePath);

        resourceType = mockResourceType();
        presenter = createDiagramEditor();
        presenter.init();
    }

    protected ClientResourceType mockResourceType() {
        final ClientResourceType resourceType = mock(ClientResourceTypeMock.class);
        when(resourceType.getSuffix()).thenReturn("bpmn");
        when(resourceType.getShortName()).thenReturn("Business Process");
        return resourceType;
    }

    protected ClientResourceType getResourceType() {
        return resourceType;
    }

    @SuppressWarnings("unchecked")
    protected AbstractProjectDiagramEditor createDiagramEditor() {
        return spy(new AbstractProjectDiagramEditor<ClientResourceTypeMock>(view,
                                                                            placeManager,
                                                                            errorPopupPresenter,
                                                                            changeTitleNotificationEvent,
                                                                            savePopUpPresenter,
                                                                            (ClientResourceTypeMock) getResourceType(),
                                                                            clientProjectDiagramService,
                                                                            sessionManager,
                                                                            sessionPresenterFactory,
                                                                            sessionCommandFactory,
                                                                            projectMenuItemsBuilder,
                                                                            onDiagramFocusEvent,
                                                                            onDiagramLostFocusEvent,
                                                                            projectMessagesListener,
                                                                            diagramClientErrorHandler,
                                                                            translationService) {
            {
                fileMenuBuilder = AbstractProjectDiagramEditorTest.this.fileMenuBuilder;
                workbenchContext = AbstractProjectDiagramEditorTest.this.workbenchContext;
                projectController = AbstractProjectDiagramEditorTest.this.projectController;
                versionRecordManager = AbstractProjectDiagramEditorTest.this.versionRecordManager;
                alertsButtonMenuItemBuilder = AbstractProjectDiagramEditorTest.this.alertsButtonMenuItemBuilder;
                kieView = AbstractProjectDiagramEditorTest.this.kieView;
                overviewWidget = AbstractProjectDiagramEditorTest.this.overviewWidget;
            }

            @Override
            protected int getCanvasWidth() {
                return 0;
            }

            @Override
            protected int getCanvasHeight() {
                return 0;
            }

            @Override
            protected String getEditorIdentifier() {
                return null;
            }
        });
    }

    @Test
    public void testMakeMenuBar() {
        doReturn(Optional.of(mock(WorkspaceProject.class))).when(workbenchContext).getActiveWorkspaceProject();
        doReturn(true).when(projectController).canUpdateProject(any());

        presenter.makeMenuBar();

        verify(fileMenuBuilder).addSave(any(MenuItem.class));
        verify(fileMenuBuilder).addCopy(any(Path.class),
                                        any(AssetUpdateValidator.class));
        verify(fileMenuBuilder).addRename(any(Path.class),
                                          any(AssetUpdateValidator.class));
        verify(fileMenuBuilder).addDelete(any(Path.class),
                                          any(AssetUpdateValidator.class));
        verify(fileMenuBuilder).addNewTopLevelMenu(alertsButtonMenuItem);
    }

    @Test
    public void testMakeMenuBarWithoutUpdateProjectPermission() {
        doReturn(Optional.of(mock(WorkspaceProject.class))).when(workbenchContext).getActiveWorkspaceProject();
        doReturn(false).when(projectController).canUpdateProject(any());

        presenter.makeMenuBar();

        verify(fileMenuBuilder,
               never()).addSave(any(MenuItem.class));
        verify(fileMenuBuilder,
               never()).addCopy(any(Path.class),
                                any(AssetUpdateValidator.class));
        verify(fileMenuBuilder,
               never()).addRename(any(Path.class),
                                  any(AssetUpdateValidator.class));
        verify(fileMenuBuilder,
               never()).addDelete(any(Path.class),
                                  any(AssetUpdateValidator.class));
        verify(fileMenuBuilder).addNewTopLevelMenu(alertsButtonMenuItem);
    }

    @Test
    public void testFormatTitle() {
        String title = "testDiagram";

        String formattedTitle = presenter.formatTitle(title);
        String suffix = getResourceType().getSuffix();
        String shortName = getResourceType().getShortName();
        assertEquals(formattedTitle,
                     "testDiagram." + suffix + " - " + shortName);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testOpen() {
        final ProjectMetadata metadata = mock(ProjectMetadata.class);
        final Overview overview = mock(Overview.class);
        final ClientSessionFactory clientSessionFactory = mock(ClientSessionFactory.class);

        when(diagram.getMetadata()).thenReturn(metadata);
        when(metadata.getTitle()).thenReturn(TITLE);
        when(metadata.getOverview()).thenReturn(overview);
        when(sessionManager.getSessionFactory(eq(metadata), eq(ClientFullSession.class))).thenReturn(clientSessionFactory);

        presenter.open(diagram);

        verify(view).showLoading();
        verify(presenter).setOriginalHash(anyInt());
        verify(presenter).destroySession();

        verify(clientSessionFactory).newSession(eq(metadata),
                                                clientFullSessionConsumerCaptor.capture());

        final Consumer<ClientFullSession> clientFullSessionConsumer = clientFullSessionConsumerCaptor.getValue();
        clientFullSessionConsumer.accept(clientFullSession);

        verify(view).setWidget(eq(sessionPresenterView));
        verify(fullSessionPresenter).withToolbar(eq(false));
        verify(fullSessionPresenter).withPalette(eq(true));
        verify(fullSessionPresenter).open(eq(diagram),
                                          eq(clientFullSession),
                                          clientSessionPresenterCallbackCaptor.capture());

        final SessionPresenter.SessionPresenterCallback clientSessionPresenterCallback = clientSessionPresenterCallbackCaptor.getValue();
        clientSessionPresenterCallback.onSuccess();

        verify(view).hideBusyIndicator();

        //Verify Overview widget was setup. It'd be nice to just verify(presenter).resetEditorPages(..) but it is protected
        verify(overviewWidget).setContent(eq(overview),
                                          eq(filePath));
        verify(kieView).clear();
        verify(kieView).addMainEditorPage(eq(view));
        verify(kieView).addOverviewPage(eq(overviewWidget),
                                        any(com.google.gwt.user.client.Command.class));

        assertEquals(fullSessionPresenter,
                     presenter.getSessionPresenter());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testOpenReadOnly() {
        final ProjectMetadata metadata = mock(ProjectMetadata.class);
        final Overview overview = mock(Overview.class);
        final ClientSessionFactory clientSessionFactory = mock(ClientSessionFactory.class);

        when(diagram.getMetadata()).thenReturn(metadata);
        when(metadata.getTitle()).thenReturn(TITLE);
        when(metadata.getOverview()).thenReturn(overview);
        when(sessionManager.getSessionFactory(eq(metadata), eq(ClientReadOnlySession.class))).thenReturn(clientSessionFactory);

        doReturn(true).when(presenter).isReadOnly();
        presenter.open(diagram);

        verify(view).showLoading();
        verify(presenter).setOriginalHash(anyInt());
        verify(presenter).destroySession();

        verify(clientSessionFactory).newSession(eq(metadata),
                                                clientReadOnlySessionConsumerCaptor.capture());

        final Consumer<ClientReadOnlySession> clientReadOnlySessionConsumer = clientReadOnlySessionConsumerCaptor.getValue();
        clientReadOnlySessionConsumer.accept(clientReadOnlySession);

        verify(view).setWidget(eq(sessionPresenterView));
        verify(readOnlySessionPresenter).withToolbar(eq(false));
        verify(readOnlySessionPresenter).withPalette(eq(false));
        verify(readOnlySessionPresenter).open(eq(diagram),
                                              eq(clientReadOnlySession),
                                              clientSessionPresenterCallbackCaptor.capture());

        final SessionPresenter.SessionPresenterCallback clientSessionPresenterCallback = clientSessionPresenterCallbackCaptor.getValue();
        clientSessionPresenterCallback.onSuccess();

        verify(view).hideBusyIndicator();

        //Verify Overview widget was setup. It'd be nice to just verify(presenter).resetEditorPages(..) but it is protected
        verify(overviewWidget).setContent(eq(overview),
                                          eq(filePath));
        verify(kieView).clear();
        verify(kieView).addMainEditorPage(eq(view));
        verify(kieView).addOverviewPage(eq(overviewWidget),
                                        any(com.google.gwt.user.client.Command.class));

        assertEquals(readOnlySessionPresenter,
                     presenter.getSessionPresenter());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCloseEditor() {
        presenter.setFullSessionPresenter(fullSessionPresenter);
        presenter.doClose();
        verify(clearStatesSessionCommand, times(1)).unbind();
        verify(switchGridSessionCommand, times(1)).unbind();
        verify(visitGraphSessionCommand, times(1)).unbind();
        verify(clearSessionCommand, times(1)).unbind();
        verify(deleteSelectionSessionCommand, times(1)).unbind();
        verify(undoSessionCommand, times(1)).unbind();
        verify(redoSessionCommand, times(1)).unbind();
        verify(validateSessionCommand, times(1)).unbind();
        verify(exportToPngSessionCommand, times(1)).unbind();
        verify(exportToJpgSessionCommand, times(1)).unbind();
        verify(exportToPdfSessionCommand, times(1)).unbind();
        verify(exportToSvgSessionCommand, times(1)).unbind();
        verify(copySelectionSessionCommand, times(1)).unbind();
        verify(pasteSelectionSessionCommand, times(1)).unbind();
        verify(cutSelectionSessionCommand, times(1)).unbind();
        verify(fullSessionPresenter, never()).clear();
        verify(fullSessionPresenter, times(1)).destroy();
    }

    @Test
    public void testInitialiseMenuBarStateForSession_Enabled() {
        presenter.initialiseMenuBarStateForSession(true);

        verify(clearItem).setEnabled(true);
        verify(visitGraphItem).setEnabled(true);
        verify(switchGridItem).setEnabled(true);
        verify(validateItem).setEnabled(true);
        verify(exportsItem).setEnabled(true);

        verify(deleteSelectionItem).setEnabled(false);
        verify(undoItem).setEnabled(false);
        verify(redoItem).setEnabled(false);
        verify(copyItem).setEnabled(false);
        verify(cutItem).setEnabled(false);
        verify(pasteItem).setEnabled(false);
    }

    @Test
    public void testInitialiseMenuBarStateForSession_Disabled() {
        presenter.initialiseMenuBarStateForSession(false);

        verify(clearItem).setEnabled(false);
        verify(visitGraphItem).setEnabled(false);
        verify(switchGridItem).setEnabled(false);
        verify(validateItem).setEnabled(false);
        verify(exportsItem).setEnabled(false);

        verify(deleteSelectionItem).setEnabled(false);
        verify(undoItem).setEnabled(false);
        verify(redoItem).setEnabled(false);
        verify(copyItem).setEnabled(false);
        verify(cutItem).setEnabled(false);
        verify(pasteItem).setEnabled(false);
    }
}
