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

var AttendeesView = AbstractView.extend({

    events: {
        'click .group-row h3': 'toggleRow',
        'click a.close': 'removeAttendee'
    },

    initialize: function () {
        _.bindAll(this, 'render', 'onNewAttendee', 'removeAttendee');
        this.model.bind('change', this.render);
        this.setElement('#attendees');
        this.render();
        Typeahead.create('#attendees-input', this.onNewAttendee);
    },

    removeAttendee: function ($event) {
        var $target = $($event.target).parents().filter('.schedule-row');
        var email = $target.attr('email');
        this.model.removeAttendee(email);
        $.gaq('attendees', 'attendee-removed', email);
    },

    onNewAttendee: function (attendee) {
        $('#attendees-input').val('');
        $('#attendee-preloader').show();
        this.model.addAttendee(attendee.address);
        $.gaq('attendees', 'attendee-added-manually', attendee.address);
    },

    render: function () {
        $.dustReplace(
            'attendees',
            {
                attendees: this.model.get('attendees')
            },
            '#attendees-list',
            function () {
                $('#attendee-preloader').hide();
            }
        );

        this.renderMatches("#attendees", this.options.suggestions);

        if (this.model.get('waiting')) {
            $('#attendee-preloader').show();
        } else {
            $('#attendee-preloader').hide();
        }
    }
});
