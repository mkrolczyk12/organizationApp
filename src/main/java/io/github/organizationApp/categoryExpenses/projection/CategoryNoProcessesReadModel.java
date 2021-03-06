package io.github.organizationApp.categoryExpenses.projection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.organizationApp.categoryExpenses.CategoryType;

@JsonIgnoreProperties({"id","processes"})
public class CategoryNoProcessesReadModel extends CategoryFullReadModel {

    public CategoryNoProcessesReadModel(final CategoryType source) {
        super(source.getId(), source.getType(), source.getDescription());
    }

}
