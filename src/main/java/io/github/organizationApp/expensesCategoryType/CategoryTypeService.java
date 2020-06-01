package io.github.organizationApp.expensesCategoryType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Service
public class CategoryTypeService {
    private static final Logger logger = LoggerFactory.getLogger(CategoryTypeService.class);
    private final CategoryTypeRepository repository;

    CategoryTypeService(final CategoryTypeRepository repository) {
        this.repository = repository;
    }

    /**
     * Create
     */
    CategoryType save(final CategoryType toCategory) {
        return repository.save(toCategory);
    }
    /**
     * Read
     */
    List<CategoryType> findAll() {
        return repository.findAll();
    }

    Page<CategoryType> findAll(Pageable page) {
        return repository.findAll(page);
    }

    Optional<CategoryType> findById(final Integer id) {
        return repository.findById(id);
    }

    CategoryType existsByType(String type) {
        return repository.existsByType(type);
    }
    /**
     * Update
     */
    CategoryType saveAndFlush(final CategoryType updatedCategoryType) {
        return repository.saveAndFlush(updatedCategoryType);
    }

    CollectionModel<CategoryType> addEachCategoryTypeLink(final List<CategoryType> result) {
        result.forEach(category -> category.add(linkTo(CategoryTypeController.class).slash(category.getId()).withSelfRel()));
        Link link1 = linkTo(CategoryTypeController.class).withSelfRel();
        Link link2 = linkTo(CategoryTypeController.class).withRel("?{sort,size,page}");
        CollectionModel<CategoryType> categoryTypeCollection = new CollectionModel(result, link1,link2);

        return categoryTypeCollection;
    }

    CollectionModel<PagedModel<CategoryType>> addEachCategoryTypeLink(final Page<CategoryType> result) {
        result.forEach(category -> category.add(linkTo(CategoryTypeController.class).slash(category.getId()).withSelfRel()));
        Link link1 = linkTo(CategoryTypeController.class).withSelfRel();
        Link link2 = linkTo(CategoryTypeController.class).withRel("?{sort,size,page}");
        CollectionModel<PagedModel<CategoryType>> categoryTypeCollection = new CollectionModel(result, link1,link2);

        return categoryTypeCollection;
    }
    /**
     * Delete
     */
    void deleteCategoryType(final Integer id) {
        repository.deleteById(id);
    }
}
