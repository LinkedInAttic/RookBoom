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

var FilterValues = Backbone.Model.extend({
    defaults: {
        day: new Date().getTime(),
        timeframe: 'Day',
        recurrence: {
            active: false
        }
    },

    initialize: function () {
        _.bindAll(this, 'addValue', 'removeValue', 'toggleValue', 'hasValue', 'resetFilter', 'fieldValues');
        var self = this;
        var recurrenceModel = this.attributes.recurrenceModel;
        recurrenceModel.bind('change', function () {
            var day = recurrenceModel.get('active')
                ? recurrenceModel.get('firstDay')
                : $.roundToDayDown(new Date().getTime());
            self.set({
                recurrence: recurrenceModel.format(),
                day: day
            });
        });
    },

    addValue: function (key, value) {
        this.set(key, _.reject(_.union([value], this.get(key)), function (v) {
            return _.isUndefined(v);
        }));
    },

    removeValue: function (key, value) {
        this.set(key, _.reject(this.get(key), function (v) {
            return _.isUndefined(v) || v === value;
        }));
    },

    hasValue: function (key, value) {
        return _.any(this.get(key), function (v) {
            return v === value;
        });
    },

    toggleValue: function (key, value) {
        if (this.hasValue(key, value)) {
            this.removeValue(key, value);
        } else {
            this.addValue(key, value);
        }
    },

    resetFilter: function (key) {
        this.set(key, []);
    },

    fieldValues: function (field) {
        var vals = this.get(field);
        vals = _.isUndefined(vals) ? [] : vals;
        return _.isArray(vals) ? vals : [vals];
    }
});