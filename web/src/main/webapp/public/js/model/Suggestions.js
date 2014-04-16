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

var Suggestions = Backbone.Model.extend({

    defaults: {
        slots: []
    },

    initialize: function () {
        _.bindAll(this, 'updateSuggestions');
        this.get('attendees').bind('change', this.updateSuggestions);
    },

    updateSuggestions: function () {
        var now = new Date().getTime();
        var slots = {};
        _.each(this.get('attendees').get('attendees'), function (att) {
            _.each(att.schedule, function (slot) {
                var val = slots[slot.from] ? slots[slot.from] : 0;
                val += (!slot.busy && slot.to > now) ? 1 : 0;
                slots[slot.from] = val;
            });
        });
        var result = [];
        var total = this.get('attendees').get('attendees').length;
        $.each(slots, function (from, count) {
            if (count === total) {
                result.push({from: from, match: 'all'});
            }
            else if (count > 0.5 * total) {
                result.push({from: from, match: 'some'});
            }
            else if (count === 0) {
                result.push({from: from, match: 'none'});
            }
            else {
                result.push({from: from, match: 'poor'});
            }
        });
        this.set('slots', result);
    }
});