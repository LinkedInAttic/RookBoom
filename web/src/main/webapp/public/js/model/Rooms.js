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

var Rooms = Backbone.Model.extend({

    defaults: {
        events: []
    },

    fromServer: false,

    fakeRoom: {
        building: 'Meet without a room',
        name: 'Anywhere',
        email: 'anywhere'
    },

    initialize: function () {
        _.bindAll(this, 'onData', 'getActiveRoomInfo', 'getRoomByEmail', 'reloadFromServer', 'getFakeRoom', 'updateSchedule');
    },

    onData: function (data) {
        if (this.get('location').getDayForLocation() != data.day) {
            return;
        }
        _.each(data.schedule, itemSchedulePatch);
        _.each(data.filtered, itemSchedulePatch);

        var user = this.attributes.auth.get('principal').user;

        _.each(data.schedule, function (s) {
            _.each(s.schedule, function (e) {
                var id = e.eventId;
                var event = data.events[id];
                if (event && event.appointment) {
                    e.appointment = event.appointment;
                    e.isOwner = user && user.account && event.appointment.owner.account === user.account;
                }
            });
        });

        this.set('events', data.events);
        this.set('timezoneOffset', data.timezoneOffset);
        this.set('scheduleOriginal', data.schedule);
        this.set('activeInfo', '');
        this.set('activeRoomInfo', '');
        this.fromServer = false;
    },

    updateSchedule: function (filter) {
        var ident = function (schedule) {
            return schedule;
        };
        var f = _.isUndefined(filter) ? ident : filter;
        var scheduleOriginal = this.get('scheduleOriginal');
        if (_.isUndefined(scheduleOriginal)) {
            return;
        }
        var fakeRoom = this.getFakeRoom(this.get('timeMask').get('timeMask'));
        var schedule = _.reject(f(scheduleOriginal), function (room) {
            return room.room.email === fakeRoom.room.email;
        });
        schedule.push(fakeRoom);
        this.set('schedule', schedule);
    },

    getRoomByEmail: function (email) {
        return _.find(
            this.get('schedule'),
            function (s) {
                return s.room.email === email
            }
        ).room;
    },

    getActiveRoomInfo: function () {
        return this.getRoomByEmail(this.get('activeRoomInfo'));
    },

    getFakeRoom: function (mask) {
        var emptySchedule = _.map(mask.frames, function (frame) {
            return {
                busy: false,
                eventId: 0,
                from: frame,
                to: frame + mask.interval
            };
        });

        return {
            maxEmpty: 0,
            room: {
                attributes: {
                    building: this.fakeRoom.building,
                    capacity: '*',
                    video: 'false'
                },
                email: this.fakeRoom.email,
                name: this.fakeRoom.name
            },
            schedule: emptySchedule
        };
    },

    reloadFromServer: function (callback) {
        this.fromServer = true;
        this.set('schedule', []);
        var filterVals = this.get('filterValues');
        var day = filterVals.get('day');
        var location = filterVals.get('location');

        var data = {
            location: location,
            day: this.get('location').getDayForLocation(),
            timeframe: this.get('filterValues').get('timeframe')
        };

        var recurrence = filterVals.get('recurrence');
        if (recurrence.active) {
            data = _.extend(data, recurrence.params);
        }

        $.ajax($.withErrorHandling({
            url: '/schedule',
            data: data,
            success: function (data) {
                callback(data);
            },
            errorMsg: 'Unable to get rooms schedule'
        }));
    }
});