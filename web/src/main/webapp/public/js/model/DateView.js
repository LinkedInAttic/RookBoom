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

var DateView = Backbone.View.extend({

    months: [
        'January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'
    ],

    days: [
        'Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'
    ],

    initialize: function () {
        _.bindAll(this, 'render');
        this.model.bind('change:day', this.render);
        this.model.bind('change:timeframe', this.render);
        this.model.bind('change:recurrence', this.render);
    },

    render: function () {
        var recurrence = this.model.get('recurrence');

        if (recurrence.active) {
            $.dustReplace('date_recurrent_view', {
                description: recurrence.description
            }, '#date-view');
        } else {
            var date = new Date(this.model.get('day'))
            $.dustReplace('date_view', {
                day: date.getDate(),
                month: this.months[date.getMonth()] + ' ' + date.getFullYear(),
                dow: this.days[date.getDay()]
            }, '#date-view');
        }
    }
});