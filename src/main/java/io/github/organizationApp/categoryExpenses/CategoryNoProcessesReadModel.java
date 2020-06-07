package io.github.organizationApp.categoryExpenses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"id","processes"})
public class CategoryNoProcessesReadModel extends CategoryFullReadModel {

    public CategoryNoProcessesReadModel(final CategoryType source) {
        super(source);
    }

}
