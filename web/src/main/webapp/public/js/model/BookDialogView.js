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

var BookDialogView = Backbone.View.extend({

    events: {
        'click .remove': 'remove',
        'click .submit': 'submit',
        'click .attendee-input a.exclude-attendee': 'removeAttendee',
        'change #book-duration': 'updateDuration',
        'click #book-duration .btn': 'updateDuration'
    },

    initialize: function () {
        _.bindAll(this, 'remove', 'submit', 'updateDuration', 'onForm', 'showDialog', 'onDialogCreate', 'removeAttendee');

        var email = this.options.$target.parents('.schedule-row').first().attr('room');
        var $slots = this.options.$target.nextUntil('.busy').andSelf();

        var $div = $('<div class="tobook-label"></div>');
        var width = $slots.first().width() + 1;
        $div.css('margin', '2px');
        $div.width(width - 5);
        $div.height($slots.first().height() - 4);
        $slots.first().append($div);

        this.room = this.model.getRoomByEmail(email);
        this.$slots = $slots;
        this.$target = this.options.$target;
        this.$bookLabel = $div;
        this.width = width;

        this.showDialog();
    },

    submit: function () {
        $('#booking-form').submit();
    },

    removeAttendee: function ($event) {
        var $target = $($event.target).parents('.attendee-input');
        var attendeeAddress = $target.find('input').val();
        $target.remove();

        this.attendees = _.filter(this.attendees, function (a) {
            return a.address != attendeeAddress;
        });

        $.gaq('attendees', 'attendee-excluded');
    },

    showDialog: function () {
        var arr = [];
        for (var i = 1; i <= this.$slots.size(); i++) {
            arr.push({
                value: i,
                label: (0.5 * i) + ' h'
            })
        }

        var from = this.$target.attr('from');
        var to = this.$target.attr('to');
        var attendees = _.map(this.options.attendees.get('attendees'), function (a) {
            return a.user;
        });
        var self = this;

        this.attendees = attendees;

        this.options.dialog.showModal(
            'booking_dialog',
            {
                free: arr,
                freeDropdown: arr.length > 6,
                room: this.room,
                from: from,
                to: to,
                realRoom: this.room.email != this.model.fakeRoom.email,
                recurrence: this.options.recurrence.attributes,
                location: self.options.siteId
            },
            {
                onShow: function () {
                    $('#book-duration').find('.btn[value="1"]').addClass('active');
                    $('#book-subject').focus();
                    $('#legend-from').text($.timeToString(from));
                    $('#legend-to').text($.timeToString(to));

                    _.forEach(self.attendees, function (att) {
                        $.dustAppend('attendee_input', att, '#required-attendees');
                    });

                    Typeahead.create('#attendees-include', function (attendee) {
                        if (!_.contains(_.map(self.attendees, function (a) {
                            return a.address;
                        }), attendee.address)) {
                            self.attendees.push(attendee);
                            $.dustAppend('attendee_input', attendee, '#required-attendees');
                        }
                    });
                },
                onHide: this.remove
            }
        );
        this.onDialogCreate(this.room);
    },

    onDialogCreate: function () {
        this.setElement($('#book-dialog'));
        var self = this;

        var recurrenceData = this.options.recurrence.get('active')
            ? this.options.recurrence.format().params
            : {};

        $('#booking-form').ajaxForm($.withErrorHandling({
            beforeSubmit: function () {
                $.gaq('booking-' + self.options.siteId.toLowerCase(), 'booking-requested', self.options.recurrence.get('active'));
                self.options.dialog.blackout();
            },
            data: recurrenceData,
            success: this.onForm,
            error: function() {
                $.gaq('booking-' + self.options.siteId.toLowerCase(), 'booking-failed');
                self.remove();
            },
            errorMsg: 'Sorry, something wrong happened during booking'
        }));
    },

    updateDuration: function ($event) {
        var n = parseInt($($event.target).val());
        this.$bookLabel.width(this.width * n - 5);
        var to = this.$slots.eq(n - 1).attr('to');
        $('#book-to').val(to);
        $('#legend-to').text($.timeToString(to));
    },

    onForm: function (data) {
        var self = this;
        var analyticsEvent = function (action, data) {
            try {
                if (data.room) {
                    $.gaq('booking-' + self.options.siteId.toLowerCase(), action, data.room.name);
                } else {
                    $.gaq('booking-' + self.options.siteId.toLowerCase(), action, 'roomless');
                }
            } catch (e) {
            }
        };

        if (data.success) {
            analyticsEvent('booking-succeed', data);
            $.successMsg('Meeting created successfully!<br/>Confirmation email sent to you');
            this.options.attendees.onDateChange();
        } else {
            analyticsEvent('booking-failed', data);
            $.errorMsg('Sorry, something wrong happened during booking');
        }
        this.options.manager.onDateChange();
        this.remove();
    },

    remove: function () {
        this.options.dialog.hide();
        this.$bookLabel.remove();
    }
});