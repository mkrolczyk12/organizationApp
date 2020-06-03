package io.github.organizationApp.expensesCategoryType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;
import java.util.stream.Collectors;
@JsonIgnoreProperties({"id","processes"})
public class PlainReadModel extends RepresentationModel<PlainReadModel> {
    private Integer id;
    private String type;
    private String description;
    private List<PlainProcessReadModel> processes;

    public PlainReadModel(CategoryType source) {
        this.id = source.getId();
        this.type = source.getType();
        this.description = source.getDescription();
        this.processes = source.getProcesses()
                .stream()
                .map(PlainProcessReadModel::new)
                .collect(Collectors.toList());
    }

    public Integer getId() {return id;}

    public String getType() {return type;}
    public void setType(final String type) {this.type = type;}

    public String getDescription() {return description;}
    public void setDescription(final String description) {this.description = description;}

    public List<PlainProcessReadModel> getProcesses() {return processes;}
    public void setProcesses(final List<PlainProcessReadModel> processes) {this.processes = processes;}
}
