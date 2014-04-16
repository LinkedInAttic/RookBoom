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

var TimeMask = Backbone.Model.extend({

    defaults: {
        timeMask: {
            frames: [],
            interval: 0
        }
    },

    initialize: function () {
        _.bindAll(this, 'onDateChange', 'onData');
        this.get('filterValues').bind('change:day', this.onDateChange);
        this.get('filterValues').bind('change:timeframe', this.onDateChange);
        this.get('filterValues').bind('change:location', this.onDateChange);
    },

    onData: function (data) {
        if (this.get('location').getDayForLocation() != data.day) {
            return;
        }
        this.set('timeMask', data.timeMask);
    },

    onDateChange: function () {
        $.ajax($.withErrorHandling({
            url: '/schedule/timemask',
            data: {
                day: this.get('location').getDayForLocation(),
                timeframe: this.get('filterValues').get('timeframe')
            },
            success: this.onData,
            errorMsg: 'Unable to get timeframe setting'
        }));
    }
});