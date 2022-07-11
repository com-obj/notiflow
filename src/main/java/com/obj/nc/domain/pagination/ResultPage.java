package com.obj.nc.domain.pagination;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@JsonAutoDetect(
        fieldVisibility = Visibility.NONE,
        setterVisibility = Visibility.NONE,
        getterVisibility = Visibility.NONE,
        isGetterVisibility = Visibility.NONE,
        creatorVisibility = Visibility.NONE
)
public class ResultPage<T> extends PageImpl<T> {

    public ResultPage(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public ResultPage(List<T> content) {
        super(content);
    }

    @JsonProperty
    public List<T> getContent() {
        return super.getContent();
    }

    @JsonProperty
    public boolean getNext() {
        return !super.isLast();
    }

    @JsonProperty
    public boolean getPrev() {
        return !super.isFirst();
    }

    @JsonProperty
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @JsonProperty
    public int getTotalPages() {
        return super.getTotalPages();
    }

    @JsonProperty
    public long getTotalElements() {
        return super.getTotalElements();
    }

    @JsonProperty
    public int getSize() {
        return super.getSize();
    }

    @JsonProperty
    public int getPage() {
        return super.getNumber() + 1;
    }
}
