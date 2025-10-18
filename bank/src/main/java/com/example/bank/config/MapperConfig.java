package com.example.bank.config;

import com.example.bank.dto.account.AccountResponse;
import com.example.bank.entity.Account;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MapperConfig {
    @Bean
    @Primary
    public ModelMapper getModelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setAmbiguityIgnored(false);
//        modelMapper.createTypeMap(Account.class, AccountResponse.class)
//                .addMappings(mapper -> mapper.skip(AccountResponse::setTransactions));

        return modelMapper;

    }
}
