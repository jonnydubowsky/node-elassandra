/**
 * Copyright © 2017 Ties BV
 *
 * This file is part of Ties.DB project.
 *
 * Ties.DB project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ties.DB project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Ties.DB project. If not, see <https://www.gnu.org/licenses/lgpl-3.0>.
 */
package network.tiesdb.service.scope.api;

import java.util.List;

public interface TiesServiceScopeRecollection {

    interface Query {

        String getTablespaceName();

        String getTableName();

        List<Selector> getSelectors();

        List<Filter> getFilters();

        interface Value {

            String getType();

            byte[] getRawValue();

            Object getValue() throws TiesServiceScopeException;

        }

        interface Field {

            String getFieldName();

        }

        interface Function {

            interface Argument {

                interface FunctionArgument extends Function, Argument {

                    @Override
                    default void accept(Visitor v) throws TiesServiceScopeException {
                        v.on(this);
                    }

                }

                interface FieldArgument extends Field, Argument {

                    @Override
                    default void accept(Visitor v) throws TiesServiceScopeException {
                        v.on(this);
                    }

                }

                interface ValueArgument extends Value, Argument {

                    @Override
                    default void accept(Visitor v) throws TiesServiceScopeException {
                        v.on(this);
                    }

                }

                interface Visitor {

                    void on(FunctionArgument a) throws TiesServiceScopeException;

                    void on(ValueArgument a) throws TiesServiceScopeException;

                    void on(FieldArgument a) throws TiesServiceScopeException;

                }

                void accept(Visitor v) throws TiesServiceScopeException;

            }

            String getName();

            List<Argument> getArguments();

        }

        public interface Selector {

            interface FunctionSelector extends Function, Selector {

                String getAlias();

                String getType();

                @Override
                default void accept(Visitor v) throws TiesServiceScopeException {
                    v.on(this);
                }

            }

            interface FieldSelector extends Field, Selector {

                @Override
                default void accept(Visitor v) throws TiesServiceScopeException {
                    v.on(this);
                }

            }

            interface Visitor {

                void on(FunctionSelector s) throws TiesServiceScopeException;

                void on(FieldSelector s) throws TiesServiceScopeException;

            }

            void accept(Visitor v) throws TiesServiceScopeException;
        }

        interface Filter extends Function {

            String getFieldName();

        }
    }

    Query getQuery();

    public interface Result {

        interface Field {

            interface HashField extends Field {

                @Override
                default <T> T accept(Visitor<T> v) throws TiesServiceScopeException {
                    return v.on(this);
                }

                byte[] getHash();

            }

            interface ValueField extends Field {

                @Override
                default <T> T accept(Visitor<T> v) throws TiesServiceScopeException {
                    return v.on(this);
                }

                Object getValue();

            }

            interface RawField extends Field {

                @Override
                default <T> T accept(Visitor<T> v) throws TiesServiceScopeException {
                    return v.on(this);
                }

                byte[] getRawValue();

            }

            interface Visitor<T> {

                T on(HashField field) throws TiesServiceScopeException;

                T on(RawField field) throws TiesServiceScopeException;

                T on(ValueField field) throws TiesServiceScopeException;

            }

            <T> T accept(Visitor<T> v) throws TiesServiceScopeException;

            String getName();

            String getType();

        }

        TiesEntryHeader getEntryHeader();

        List<Field> getEntryFields();

        List<Field> getComputedFields();

    }

    void addResult(Result result) throws TiesServiceScopeException;
}