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

window.preloader = '<div class="preloader"><img src="/public/i/preloader.gif"/></div>';

window.SILENT = {silent: true};

window.itemSchedulePatch = function (itemSchedule) {
    var first = {eventId: 0};
    var count = 1;
    var maxEmpty = 0;

    var patch = function (slot, count) {
        if (slot.busy) {
            slot.length = count;
            slot.firstFrame = true;
        } else {
            maxEmpty = Math.max(maxEmpty, count);
        }
    };

    _.each(itemSchedule.schedule, function (slot) {
        if (slot.eventId == first.eventId) {
            count++;
            return;
        }
        patch(first, count);
        first = slot;
        count = 1;
    });
    patch(first, count);

    itemSchedule.maxEmpty = maxEmpty;
};

var templates = [
    'location_select',
    'schedule-list',
    'autocomplete',
    'login_dialog',
    'dialog_base',
    'booking_dialog',
    'attendee_input',
    'date_picker',
    'date_recurrent_view',
    'date_view',
    'login_form',
    'recurrence',
    'user_menu',
    'attendees',
    'time-mask',
    'book_info',
    'room-info',
    'location',
    'filters',
    'dialog',
    'alert',
    'about',
    'auth',
    'tip'
];

$.dustPreload(templates, function () {
    //authConfig var initialized in the index.dust template
    var recurrence = new Recurrence();
    var auth = new Auth({principal: ROOKBOOM.authConfig});
    var filterValues = new FilterValues({recurrenceModel: recurrence});
    var location = new Location({config: ROOKBOOM.locationsConfig, defaultLocationId: ROOKBOOM.defaultLocationId, filterValues: filterValues});
    var timeMask = new TimeMask({filterValues: filterValues, location: location});
    var attendees = new Attendees({filterValues: filterValues, location: location});
    var suggestions = new Suggestions({attendees: attendees});
    var rooms = new Rooms({filterValues: filterValues, location: location, timeMask: timeMask, auth: auth});
    var filterManager = new FilterManager({filterValues: filterValues, rooms: rooms, config: ROOKBOOM.filtersConfig});

    var dialog = new DialogView();
    var loginView = new LoginView({model: auth, attendees: attendees, dialog: dialog});
    var authView = new AuthView({model: auth, loginView: loginView, dialog: dialog});
    var locationView = new LocationView({model: location, filterValues: filterValues, dialog: dialog});
    var recurrenceView = new RecurrenceView({model: recurrence, filterValues: filterValues, location: location});
    var filtersView = new FiltersView({model: filterValues, manager: filterManager, rooms: rooms, recurrenceView: recurrenceView, dialog: dialog});
    var dateView = new DateView({model: filterValues});
    var timeMaskView = new TimeMaskView({model: timeMask, location: location, filterValues: filterValues});
    var attendeesView = new AttendeesView({model: attendees, timeMask: timeMask, suggestions: suggestions});
    var roomsView = new RoomsView({model: rooms, auth: auth, loginView: loginView, suggestions: suggestions, attendees: attendees, manager: filterManager, location: location, recurrence: recurrence, dialog: dialog});
    var hashView = new HashView({attendees: attendees, filterValues: filterValues, filterManager: filterManager, location: location, filtersView: filtersView, auth: auth, recurrence: recurrence});

    $('#attendees-input').focus();
});