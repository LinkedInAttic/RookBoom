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

var FilterManager = Backbone.Model.extend({

    filtersMap: {},

    attributeFilterBase: {
        apply: function (filterValues, scheduleOriginal) {
            var self = this;
            // looking for a match with at least one of the filter values
            var values = filterValues.fieldValues(this.id);

            // special case: if there is no values for a filter then no filtering at all applied
            if (values.length === 0) {
                return scheduleOriginal;
            }

            // using _.reduce to get union of all filtering results
            return _.reduce(_.map(values, function (val) {
                // applying a single filter
                return _.filter(scheduleOriginal, self.filterFunc(val));
            }), function (union, filteredByOneFilter) {
                return _.union(union, filteredByOneFilter);
            }, []);
        }
    },

    initialize: function () {
        _.bindAll(this, 'onFilterValueUpdate', 'filters', 'presentation', 'onDateChange', 'doFilter', 'onNewRoomsData');

        this.attributeFilters = {
            value: new ValueKind(this.get('filterValues'), this.get('rooms')),
            range: new RangeKind(this.get('filterValues')),
            binary: new BinaryKind(this.get('filterValues'))
        };

        this.filtersMap = this.filters();

        var self = this;
        _.each(_.keys(this.filtersMap), function (field) {
            self.get('filterValues').bind('change:' + field, self.onFilterValueUpdate);
        });
        this.get('filterValues').bind('change:day', this.onDateChange);
        this.get('filterValues').bind('change:timeframe', this.onDateChange);
        this.get('filterValues').bind('change:location', this.onDateChange);
        this.get('filterValues').bind('change:recurrence', this.onDateChange);
    },

    onDateChange: function () {
        this.get('rooms').reloadFromServer(this.onNewRoomsData);
    },

    onNewRoomsData: function (data) {
        data.filtered = this.doFilter(data.schedule);
        this.get('rooms').onData(data);
        this.onFilterValueUpdate();
    },

    configCall: function (self, name) {
        return function (conf) {
            return self.attributeFilters[conf.kind][name](conf);
        };
    },

    presentation: function () {
        return _.map(this.attributes.config, this.configCall(this, 'presentation'));
    },

    filters: function () {
        var self = this;

        var filtersMap = _.reduce(_.map(_.flatten(_.map(self.attributes.config,
            this.configCall(this, 'filter')
        )), function (f) {
            return $.extend(f, self.attributeFilterBase);
        }), function (filtersMap, f) {
            filtersMap[f.id] = f;
            return filtersMap;
        }, {});

        filtersMap['name'] = $.extend({
            id: 'name',
            filterFunc: function (value) {
                var pattern = value.toLowerCase();
                return function (i) {
                    return i.room.name.toLowerCase().indexOf(pattern) >= 0
                };
            }
        }, self.attributeFilterBase);

        return filtersMap;
    },

    onFilterValueUpdate: function () {
        this.get('rooms').updateSchedule(this.doFilter);
    },

    doFilter: function (scheduleOriginal) {
        var filterValues = this.get('filterValues');
        return _.reduce(_.values(this.filtersMap), function (filteredSoFar, filter) {
            return filter.apply(filterValues, filteredSoFar);
        }, scheduleOriginal);
    }
});

var attributeValueFilter = function (field) {
    return {
        id: field,
        filterFunc: function (value) {
            return function (s) {
                return s.room.attributes[field] === value;
            };
        }
    };
};

var ValueKind = function (filterValues, rooms) {

    this.filterValues = filterValues;

    this.rooms = rooms;

    this.filter = function (item) {
        return [attributeValueFilter(item.attributes.field)];
    };

    this.presentation = function (filter) {
        var schedule = this.rooms.get('scheduleOriginal');
        var field = filter.attributes.field;
        var values = _.sortBy(_.uniq(_.map(schedule, function (item) {
            return item.room.attributes[field];
        })), function (i) {
            try {
                return parseInt(i);
            } catch (e) {
                return i;
            }
        });

        var actualVals = _.reduce(this.filterValues.fieldValues(field), function (actualVals, val) {
            actualVals[val] = true;
            return actualVals;
        }, {});

        var result = _.map(values, function (val) {
            return {
                label: val,
                field: field,
                value: val,
                active: !_.isUndefined(actualVals[val])
            };
        });

        return $.extend({values: result, any: _.keys(actualVals).length === 0}, filter);
    };
};

var RangeKind = function (filterValues) {

    this.filterValues = filterValues;

    this.filter = function (item) {
        var field = item.attributes.field;
        var range = item.attributes.range;
        return [
            {
                id: item.attributes.field,
                filterFunc: function (value) {
                    var interval = range[value];
                    return function (s) {
                        var toCheck = parseInt(s.room.attributes[field]);
                        return interval[0] <= toCheck && toCheck < interval[1];
                    };
                }
            }
        ];
    };

    this.presentation = function (filter) {
        var result = [];
        var field = filter.attributes.field;

        var actualVals = _.reduce(this.filterValues.fieldValues(field), function (actualVals, val) {
            actualVals[val] = true;
            return actualVals;
        }, {});

        $.each(filter.attributes.range, function (label, value) {
            result.push({
                label: label,
                field: field,
                value: label,
                active: !_.isUndefined(actualVals[label])
            });
        });

        return $.extend({values: result, any: _.keys(actualVals).length === 0}, filter);
    };
};

var BinaryKind = function (filterValues) {

    this.filterValues = filterValues;

    this.filter = function (item) {
        return _.reduce(_.keys(item.attributes), function (result, field) {
            result.push(attributeValueFilter(field));
            return result;
        }, []);
    };

    this.presentation = function (filter) {
        var self = this;
        var result = [];
        var any = true;
        $.each(filter.attributes, function (field, label) {
            var active = self.filterValues.fieldValues(field).length > 0;
            any = active ? false : any;
            result.push({
                label: label,
                field: field,
                value: 'true',
                active: active
            });
        });
        return $.extend({values: result, any: any}, filter);
    };
};
