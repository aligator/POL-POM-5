/*
 * Copyright (C) 2015-2017 PÂRIS Quentin
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.phoenicis.javafx.views.mainwindow.engines;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TabPane;
import org.phoenicis.engines.dto.EngineCategoryDTO;
import org.phoenicis.engines.dto.EngineDTO;
import org.phoenicis.engines.dto.EngineVersionDTO;
import org.phoenicis.javafx.settings.JavaFxSettingsManager;
import org.phoenicis.javafx.views.common.ThemeManager;
import org.phoenicis.javafx.views.common.lists.ExpandedList;
import org.phoenicis.javafx.views.common.lists.MappedList;
import org.phoenicis.javafx.views.common.lists.PhoenicisFilteredList;
import org.phoenicis.javafx.views.common.widgets.lists.CombinedListWidget;
import org.phoenicis.javafx.views.mainwindow.ui.MainWindowView;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.phoenicis.configuration.localisation.Localisation.tr;

public class EnginesView extends MainWindowView<EnginesSidebar> {
    private final EnginesFilter filter;

    private TabPane availableEngines;

    private EnginePanel currentEnginePanel;

    private ObservableList<EngineCategoryDTO> engineCategories;

    private ExpandedList<EngineSubCategoryTab, EngineCategoryDTO> engineSubCategoryTabs;
    private SortedList<EngineSubCategoryTab> sortedEngineSubTabs;
    private PhoenicisFilteredList<EngineSubCategoryTab> filteredEngineSubTabs;

    private MappedList<CombinedListWidget<EngineVersionDTO>, EngineSubCategoryTab> mappedListWidgets;

    private Consumer<EngineDTO> setOnInstallEngine = (engine) -> {
    };
    private Consumer<EngineDTO> setOnDeleteEngine = (engine) -> {
    };

    public EnginesView(ThemeManager themeManager, String enginesPath, JavaFxSettingsManager javaFxSettingsManager) {
        super(tr("Engines"), themeManager);

        this.filter = new EnginesFilter(enginesPath);

        /*
         * initialize the engine categories list
         */
        this.engineCategories = FXCollections.observableArrayList();

        // initialize the engines sub category tabs
        this.engineSubCategoryTabs = new ExpandedList<>(engineCategories,
                engineCategory -> engineCategory.getSubCategories().stream()
                        .map(engineSubCategory -> {
                            EngineSubCategoryTab result = new EngineSubCategoryTab(engineCategory, engineSubCategory,
                                    enginesPath,
                                    filter);

                            result.setOnSelectEngine(this::showEngineDetails);

                            return result;
                        }).collect(Collectors.toList()));
        // sort the engine sub category tabs alphabetically
        this.sortedEngineSubTabs = new SortedList<>(engineSubCategoryTabs,
                Comparator.comparing(engineSubCategoryTab -> engineSubCategoryTab.getEngineSubCategory().getName()));
        // filter the engine sub category tabs, so that only the visible tabs remain
        this.filteredEngineSubTabs = new PhoenicisFilteredList<>(sortedEngineSubTabs, filter::filter);
        this.filter.addOnFilterChanged(filteredEngineSubTabs::trigger);

        this.mappedListWidgets = new MappedList<>(filteredEngineSubTabs, EngineSubCategoryTab::getEngineVersionsView);

        this.sidebar = new EnginesSidebar(mappedListWidgets, filter, javaFxSettingsManager);

        this.sidebar.setOnCategorySelection(filter::setSelectedEngineCategory);

        this.initFailure();
        this.initWineVersions();

        this.sidebar.bindEngineCategories(engineCategories);
        Bindings.bindContent(availableEngines.getTabs(), filteredEngineSubTabs);

        this.setSidebar(this.sidebar);
    }

    public void setOnInstallEngine(Consumer<EngineDTO> onInstallEngine) {
        this.setOnInstallEngine = onInstallEngine;
    }

    public void setOnDeleteEngine(Consumer<EngineDTO> onDeleteEngine) {
        this.setOnDeleteEngine = onDeleteEngine;
    }

    private void initFailure() {

    }

    private void initWineVersions() {
        availableEngines = new TabPane();
        availableEngines.getStyleClass().add("rightPane");

        availableEngines.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    }

    // TODO: delete this method because it doesn't do what it promises, namely showing the wine versions tab
    @Deprecated
    public void showWineVersions() {
        setCenter(availableEngines);
    }

    public void populate(List<EngineCategoryDTO> engineCategoryDTOS) {
        Platform.runLater(() -> {
            this.engineCategories.setAll(engineCategoryDTOS);

            if (!engineCategoryDTOS.isEmpty()) {
                this.sidebar.selectFirstEngineCategory();
            }

            this.closeDetailsView();
            this.setCenter(availableEngines);
        });
    }

    private void showEngineDetails(EngineDTO engineDTO) {
        currentEnginePanel = new EnginePanel(engineDTO);
        currentEnginePanel.setOnClose(this::closeDetailsView);
        currentEnginePanel.setOnEngineInstall(this::installEngine);
        currentEnginePanel.setOnEngineDelete(this::deleteEngine);

        this.showDetailsView(currentEnginePanel);
    }

    private void installEngine(EngineDTO engineDTO) {
        this.setOnInstallEngine.accept(engineDTO);
    }

    private void deleteEngine(EngineDTO engineDTO) {
        this.setOnDeleteEngine.accept(engineDTO);
    }

}
