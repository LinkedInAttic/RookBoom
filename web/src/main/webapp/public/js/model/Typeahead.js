/*
 * (c) Copyright 2014 LinkedIn Corp. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var Typeahead = {

    create: function (element, handler) {
        $(element).typeahead(this.config(handler));
    },

    config: function (handler) {
        return {
            source: function (query, process) {
                var typeahead = this;
                $.ajax({
                    url: '/users/search',
                    data: {
                        query: query
                    },
                    success: function (data) {
                        typeahead.results = _.reduce(data.users, function (memo, item) {
                            memo[item.address] = item;
                            return memo;
                        }, {});
                        process(_.pluck(data.users, 'address'));
                    }
                });
            },
            matcher: function () {
                return true;
            },
            highlighter: function (address) {
                var query = this.query;
                var item = this.results[address];
                return $.dustRender('autocomplete', {
                    name: Typeahead.highlihgt(item.displayName, query),
                    email: Typeahead.highlihgt(item.address, query)
                });
            },
            sorter: function (items) {
                return items;
            },
            updater: function (address) {
                handler(this.results[address]);
                return '';
            }
        };
    },

    highlihgt: function (string, pattern, from) {
        from = from === undefined ? 0 : from;
        var prefix = '<b class="yui3-highlight">';
        var suffix = '</b>';
        var lcString = string.toLowerCase();
        var lcPattern = pattern.toLowerCase();
        var i = lcString.indexOf(lcPattern, from);
        if (i < 0) {
            return string;
        }
        var a = string.substr(0, i);
        var b = string.substr(i, pattern.length);
        var c = string.substr(i + pattern.length);
        return Typeahead.highlihgt(a + prefix + b + suffix + c, pattern, i + prefix.length + suffix.length);
    }
};