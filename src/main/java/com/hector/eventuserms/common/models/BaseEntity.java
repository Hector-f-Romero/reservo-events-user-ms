package com.hector.eventuserms.common.models;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
@FilterDef(name = "activeFilter", parameters = @ParamDef(name = "isActive", type = Boolean.class))
@Filter(name = "activeFilter", condition = "isActive = :isActive")
public abstract class BaseEntity {

    @CreationTimestamp
    @JsonIgnore
    @Column(name = "\"createdAt\"", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant createdAt;

    @UpdateTimestamp
    @JsonIgnore
    @Column(name = "\"updatedAt\"", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant updatedAt;

    @Column(name = "\"isActive\"", nullable = false)
    @JsonIgnore
    private Boolean isActive = true;
}
