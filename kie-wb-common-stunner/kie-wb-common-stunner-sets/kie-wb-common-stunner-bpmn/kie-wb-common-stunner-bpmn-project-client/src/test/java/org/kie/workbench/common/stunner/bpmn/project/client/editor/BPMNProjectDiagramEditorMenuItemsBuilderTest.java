/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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
package org.kie.workbench.common.stunner.bpmn.project.client.editor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.stunner.client.widgets.popups.PopupUtil;
import org.kie.workbench.common.stunner.core.client.i18n.ClientTranslationService;
import org.kie.workbench.common.stunner.core.i18n.CoreTranslationMessages;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BPMNProjectDiagramEditorMenuItemsBuilderTest {

    @Mock
    private ClientTranslationService translationService;

    @Mock
    private PopupUtil popupUtil;

    private BPMNProjectDiagramEditorMenuItemsBuilder builder;

    @Before
    public void setup() {
        this.builder = new BPMNProjectDiagramEditorMenuItemsBuilder(translationService, popupUtil);

        when(translationService.getValue(anyString())).thenAnswer(i -> i.getArguments()[0].toString());
    }

    @Test
    public void testExportAsRawLabel() {
        assertEquals(CoreTranslationMessages.EXPORT_BPMN,
                     builder.getExportLabelToRawFormatIfSupported().get());
    }
}
