package org.example.expensetracker.ledger.config;

import org.example.expensetracker.ledger.entity.ExpenseCategory;
import org.example.expensetracker.ledger.entity.IncomeSource;
import org.example.expensetracker.ledger.entity.LedgerEntryType;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class LedgerEnumConversionConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new LedgerEntryTypeParamConverter());
        registry.addConverter(new ExpenseCategoryParamConverter());
        registry.addConverter(new IncomeSourceParamConverter());
    }

    static class LedgerEntryTypeParamConverter implements Converter<String, LedgerEntryType> {
        @Override
        public LedgerEntryType convert(String source) {
            return LedgerEntryType.from(source);
        }
    }

    static class ExpenseCategoryParamConverter implements Converter<String, ExpenseCategory> {
        @Override
        public ExpenseCategory convert(String source) {
            return ExpenseCategory.from(source);
        }
    }

    static class IncomeSourceParamConverter implements Converter<String, IncomeSource> {
        @Override
        public IncomeSource convert(String source) {
            return IncomeSource.from(source);
        }
    }
}
