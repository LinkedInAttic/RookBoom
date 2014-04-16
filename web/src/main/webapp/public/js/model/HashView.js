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

function parseDate(hash, def) {
    try {
        var d = hash.split('-');
        var t = new Date(
            parseInt(d[0], 10),
            parseInt(d[1], 10) - 1,
            parseInt(d[2], 10),
            1
        );
        return t.getTime() && t.getTime() > def ? t.getTime() : def;
    } catch (e) {
        return def;
    }
}

var HashView = Backbone.View.extend({

    initialize: function () {
        _.bindAll(
            this,
            'render',
            'parseHash',
            'dateHashBuilder',
            'attendeesHashBuilder',
            'filtersHashBuilder',
            'locationHashBuilder',
            'recurrenceHashBuilder',
            'timeMaskHashBuilder'
        );
        this.options.attendees.bind('change', this.render);
        this.options.filterValues.bind('change', this.render);
        this.parseHash();
    },

    dateHashBuilder: function () {
        var day = this.options.filterValues.get('day');
        return $.timeToDateString(day);
    },

    attendeesHashBuilder: function () {
        var attendeesArr = _.map(this.options.attendees.get('attendees'), function (att) {
            return att.user.address.replace('@linkedin.com', '');
        });
        if (attendeesArr.length === 0) {
            return '';
        }
        var attendeesStr = attendeesArr.join(',');
        return 'attendees=' + attendeesStr;
    },

    filtersHashBuilder: function () {
        var filterValues = this.options.filterValues;
        var value = _.chain(this.options.filterManager.filtersMap).keys().map(function (field) {
            return {
                field: field,
                vals: filterValues.fieldValues(field)
            };
        }).reject(function (item) {
                return item.vals.length === 0;
            }).map(function (item) {
                return item.field + ':' + JSON.stringify(item.vals);
            }).value().join(';');

        return value.length === 0 ? '' : 'filters=' + value;
    },

    locationHashBuilder: function () {
        var value = this.options.filterValues.get('location');
        return 'at=' + value;
    },

    recurrenceHashBuilder: function () {
        var recurrence = this.options.recurrence.attributes;
        return recurrence.active ? 'recurrence=' + JSON.stringify(recurrence) : '';
    },

    timeMaskHashBuilder: function () {
        var value = this.options.filterValues.get('timeframe');
        return value && value !== 'Day' ? 'timeframe=' + value : '';
    },

    render: function () {
        var values = _.reject([
            this.dateHashBuilder(),
            this.attendeesHashBuilder(),
            this.filtersHashBuilder(),
            this.locationHashBuilder(),
            this.recurrenceHashBuilder(),
            this.timeMaskHashBuilder()
        ], function (str) {
            return _.isUndefined(str) || str.length === 0;
        });
        location.hash = values.join('&');
    },

    parseHash: function () {
        var self = this;
        var hashString = location.hash.replace('#', 'date=');

        var parsers = {
            date: function (val) {
                var time = parseDate(val, new Date().getTime());
                self.options.filterValues.set('day', $.roundToDayDown(time), SILENT);
            },
            attendees: function (val) {
                var attendees = _.map(val.split(','), function (name) {
                    return name + '@linkedin.com';
                });
                self.options.attendees.updateByEmailsString(attendees.join());
            },
            filters: function (val) {
                _.chain(val.split(';')).each(function (item) {
                    var parts = item.split(':');
                    var field = parts[0];
                    var vals = JSON.parse(decodeURIComponent(parts[1]));
                    self.options.filterValues.set(field, vals, SILENT);
                });
                self.options.filtersView.toggleFilters();
            },
            at: function (val) {
                self.options.location.setById(val);
            },
            timeframe: function (val) {
                self.options.filterValues.set('timeframe', val, SILENT);
            },
            recurrence: function (val) {
                var rec = JSON.parse(decodeURIComponent(val));
                self.options.recurrence.set(rec);
            }
        };

        var hash = _.reduce(hashString.split('&'), function (o, h) {
            var parts = h.split('=');
            o[parts[0]] = parts[1];
            return o;
        }, {date: $.timeToDateString(new Date().getTime())});

        $.each(hash, function (key, value) {
            if (parsers[key]) {
                parsers[key](value);
            }
        });

        var principal = this.options.auth.get('principal');
        if (principal.loggedIn) {
            self.options.attendees.addAttendee(principal.user.address);
        }

        self.options.filterValues.change();
    }
});