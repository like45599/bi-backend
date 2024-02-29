package com.yupi.moonBI.repository;


import com.yupi.moonBI.model.document.Chart;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Component;


import java.util.List;

@Component
public interface ChartRepository extends MongoRepository<Chart, String> {

    @Query("{'userId': ?0}")
    Page<Chart> findAllByUserId(long userId, Pageable pageable);

    @Query("{'chartId': ?0}")
    List<Chart> findAllByChartId(long chartId);

    long deleteAllByChartId(long chartId);

    Chart findByChartId(long chartId);

}
