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

package com.microsoft.exchange.utils;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This file is generated from CLDR data
 * http://unicode.org/repos/cldr/trunk/common/supplemental/windowsZones.xml
 */
class WindowsZonesMapping {

    private static final Map<String, String> STANDARD_TO_WINDOWS = new HashMap<String, String>();

    private static final Map<String, Set<String>> WINDOWS_TO_STANDARD = new HashMap<String, Set<String>>();

    public static String getWindowsId(String standardId) {
        String windowsId = STANDARD_TO_WINDOWS.get(standardId);
        if (windowsId == null) {
            throw new IllegalArgumentException("No windows id found for the standard id '" + standardId + "'");
        }
        return windowsId;
    }

    public static Set<String> getStandardIds(String windowsId) {
        Set<String> standardIds = WINDOWS_TO_STANDARD.get(windowsId);
        if (standardIds == null) {
            throw new IllegalArgumentException("No standard ids found for the windows id '" + windowsId + "'");
        }
        return standardIds;
    }

    private static void addMapping(String windowsId, String territory, String standardIds) {
        // mapping file contains multiple standard ids separated by the space symbol
        for (String standardId : standardIds.split(" ")) {
            addMapping(windowsId, standardId);
        }
    }

    private static void addMapping(String windowsId, String standardId) {
        // add a standard to windows mapping
        STANDARD_TO_WINDOWS.put(standardId, windowsId);
        // add a windows to standard mapping
        Set<String> standardIds = WINDOWS_TO_STANDARD.get(windowsId);
        if (standardIds == null) {
            standardIds = new HashSet<String>();
            WINDOWS_TO_STANDARD.put(windowsId, standardIds);
        }
        standardIds.add(standardId);
    }

    static {

        // (UTC-12:00) International Date Line West  
        addMapping("Dateline Standard Time", "001", "Etc/GMT+12");
        addMapping("Dateline Standard Time", "ZZ", "Etc/GMT+12");
        // (UTC-11:00) Coordinated Universal Time-11  
        addMapping("UTC-11", "001", "Etc/GMT+11");
        addMapping("UTC-11", "AS", "Pacific/Pago_Pago");
        addMapping("UTC-11", "NU", "Pacific/Niue");
        addMapping("UTC-11", "UM", "Pacific/Midway");
        addMapping("UTC-11", "ZZ", "Etc/GMT+11");
        // (UTC-10:00) Hawaii  
        addMapping("Hawaiian Standard Time", "001", "Pacific/Honolulu");
        addMapping("Hawaiian Standard Time", "CK", "Pacific/Rarotonga");
        addMapping("Hawaiian Standard Time", "PF", "Pacific/Tahiti");
        addMapping("Hawaiian Standard Time", "UM", "Pacific/Johnston");
        addMapping("Hawaiian Standard Time", "US", "Pacific/Honolulu");
        addMapping("Hawaiian Standard Time", "ZZ", "Etc/GMT+10");
        // (UTC-09:00) Alaska  
        addMapping("Alaskan Standard Time", "001", "America/Anchorage");
        addMapping("Alaskan Standard Time", "US", "America/Anchorage America/Juneau America/Nome America/Sitka America/Yakutat");
        // (UTC-08:00) Baja California  
        addMapping("Pacific Standard Time (Mexico)", "001", "America/Santa_Isabel");
        addMapping("Pacific Standard Time (Mexico)", "MX", "America/Santa_Isabel");
        // (UTC-08:00) Pacific Time (US & Canada)  
        addMapping("Pacific Standard Time", "001", "America/Los_Angeles");
        addMapping("Pacific Standard Time", "CA", "America/Vancouver America/Dawson America/Whitehorse");
        addMapping("Pacific Standard Time", "MX", "America/Tijuana");
        addMapping("Pacific Standard Time", "US", "America/Los_Angeles");
        addMapping("Pacific Standard Time", "ZZ", "PST8PDT");
        // (UTC-07:00) Arizona  
        addMapping("US Mountain Standard Time", "001", "America/Phoenix");
        addMapping("US Mountain Standard Time", "CA", "America/Dawson_Creek America/Creston");
        addMapping("US Mountain Standard Time", "MX", "America/Hermosillo");
        addMapping("US Mountain Standard Time", "US", "America/Phoenix");
        addMapping("US Mountain Standard Time", "ZZ", "Etc/GMT+7");
        // (UTC-07:00) Chihuahua, La Paz, Mazatlan  
        addMapping("Mountain Standard Time (Mexico)", "001", "America/Chihuahua");
        addMapping("Mountain Standard Time (Mexico)", "MX", "America/Chihuahua America/Mazatlan");
        // (UTC-07:00) Mountain Time (US & Canada)  
        addMapping("Mountain Standard Time", "001", "America/Denver");
        addMapping("Mountain Standard Time", "CA", "America/Edmonton America/Cambridge_Bay America/Inuvik America/Yellowknife");
        addMapping("Mountain Standard Time", "MX", "America/Ojinaga");
        addMapping("Mountain Standard Time", "US", "America/Denver America/Boise America/Shiprock");
        addMapping("Mountain Standard Time", "ZZ", "MST7MDT");
        // (UTC-06:00) Central America  
        addMapping("Central America Standard Time", "001", "America/Guatemala");
        addMapping("Central America Standard Time", "BZ", "America/Belize");
        addMapping("Central America Standard Time", "CR", "America/Costa_Rica");
        addMapping("Central America Standard Time", "EC", "Pacific/Galapagos");
        addMapping("Central America Standard Time", "GT", "America/Guatemala");
        addMapping("Central America Standard Time", "HN", "America/Tegucigalpa");
        addMapping("Central America Standard Time", "NI", "America/Managua");
        addMapping("Central America Standard Time", "SV", "America/El_Salvador");
        addMapping("Central America Standard Time", "ZZ", "Etc/GMT+6");
        // (UTC-06:00) Central Time (US & Canada)  
        addMapping("Central Standard Time", "001", "America/Chicago");
        addMapping("Central Standard Time", "CA", "America/Winnipeg America/Rainy_River America/Rankin_Inlet America/Resolute");
        addMapping("Central Standard Time", "MX", "America/Matamoros");
        addMapping("Central Standard Time", "US", "America/Chicago America/Indiana/Knox America/Indiana/Tell_City America/Menominee America/North_Dakota/Beulah America/North_Dakota/Center America/North_Dakota/New_Salem");
        addMapping("Central Standard Time", "ZZ", "CST6CDT");
        // (UTC-06:00) Guadalajara, Mexico City, Monterrey  
        addMapping("Central Standard Time (Mexico)", "001", "America/Mexico_City");
        addMapping("Central Standard Time (Mexico)", "MX", "America/Mexico_City America/Bahia_Banderas America/Cancun America/Merida America/Monterrey");
        // (UTC-06:00) Saskatchewan  
        addMapping("Canada Central Standard Time", "001", "America/Regina");
        addMapping("Canada Central Standard Time", "CA", "America/Regina America/Swift_Current");
        // (UTC-05:00) Bogota, Lima, Quito  
        addMapping("SA Pacific Standard Time", "001", "America/Bogota");
        addMapping("SA Pacific Standard Time", "CA", "America/Coral_Harbour");
        addMapping("SA Pacific Standard Time", "CO", "America/Bogota");
        addMapping("SA Pacific Standard Time", "EC", "America/Guayaquil");
        addMapping("SA Pacific Standard Time", "JM", "America/Jamaica");
        addMapping("SA Pacific Standard Time", "KY", "America/Cayman");
        addMapping("SA Pacific Standard Time", "PA", "America/Panama");
        addMapping("SA Pacific Standard Time", "PE", "America/Lima");
        addMapping("SA Pacific Standard Time", "ZZ", "Etc/GMT+5");
        // (UTC-05:00) Eastern Time (US & Canada)  
        addMapping("Eastern Standard Time", "001", "America/New_York");
        addMapping("Eastern Standard Time", "BS", "America/Nassau");
        addMapping("Eastern Standard Time", "CA", "America/Toronto America/Iqaluit America/Montreal America/Nipigon America/Pangnirtung America/Thunder_Bay");
        addMapping("Eastern Standard Time", "CU", "America/Havana");
        addMapping("Eastern Standard Time", "HT", "America/Port-au-Prince");
        addMapping("Eastern Standard Time", "TC", "America/Grand_Turk");
        addMapping("Eastern Standard Time", "US", "America/New_York America/Detroit America/Indiana/Petersburg America/Indiana/Vincennes America/Indiana/Winamac America/Kentucky/Monticello America/Louisville");
        addMapping("Eastern Standard Time", "ZZ", "EST5EDT");
        // (UTC-05:00) Indiana (East)  
        addMapping("US Eastern Standard Time", "001", "America/Indianapolis");
        addMapping("US Eastern Standard Time", "US", "America/Indianapolis America/Indiana/Marengo America/Indiana/Vevay");
        // (UTC-04:30) Caracas  
        addMapping("Venezuela Standard Time", "001", "America/Caracas");
        addMapping("Venezuela Standard Time", "VE", "America/Caracas");
        // (UTC-04:00) Asuncion  
        addMapping("Paraguay Standard Time", "001", "America/Asuncion");
        addMapping("Paraguay Standard Time", "PY", "America/Asuncion");
        // (UTC-04:00) Atlantic Time (Canada)  
        addMapping("Atlantic Standard Time", "001", "America/Halifax");
        addMapping("Atlantic Standard Time", "BM", "Atlantic/Bermuda");
        addMapping("Atlantic Standard Time", "CA", "America/Halifax America/Glace_Bay America/Goose_Bay America/Moncton");
        addMapping("Atlantic Standard Time", "GL", "America/Thule");
        // (UTC-04:00) Cuiaba  
        addMapping("Central Brazilian Standard Time", "001", "America/Cuiaba");
        addMapping("Central Brazilian Standard Time", "BR", "America/Cuiaba America/Campo_Grande");
        // (UTC-04:00) Georgetown, La Paz, Manaus, San Juan  
        addMapping("SA Western Standard Time", "001", "America/La_Paz");
        addMapping("SA Western Standard Time", "AG", "America/Antigua");
        addMapping("SA Western Standard Time", "AI", "America/Anguilla");
        addMapping("SA Western Standard Time", "AW", "America/Aruba");
        addMapping("SA Western Standard Time", "BB", "America/Barbados");
        addMapping("SA Western Standard Time", "BL", "America/St_Barthelemy");
        addMapping("SA Western Standard Time", "BO", "America/La_Paz");
        addMapping("SA Western Standard Time", "BQ", "America/Kralendijk");
        addMapping("SA Western Standard Time", "BR", "America/Manaus America/Boa_Vista America/Eirunepe America/Porto_Velho America/Rio_Branco");
        addMapping("SA Western Standard Time", "CA", "America/Blanc-Sablon");
        addMapping("SA Western Standard Time", "CW", "America/Curacao");
        addMapping("SA Western Standard Time", "DM", "America/Dominica");
        addMapping("SA Western Standard Time", "DO", "America/Santo_Domingo");
        addMapping("SA Western Standard Time", "GD", "America/Grenada");
        addMapping("SA Western Standard Time", "GP", "America/Guadeloupe");
        addMapping("SA Western Standard Time", "GY", "America/Guyana");
        addMapping("SA Western Standard Time", "KN", "America/St_Kitts");
        addMapping("SA Western Standard Time", "LC", "America/St_Lucia");
        addMapping("SA Western Standard Time", "MF", "America/Marigot");
        addMapping("SA Western Standard Time", "MQ", "America/Martinique");
        addMapping("SA Western Standard Time", "MS", "America/Montserrat");
        addMapping("SA Western Standard Time", "PR", "America/Puerto_Rico");
        addMapping("SA Western Standard Time", "SX", "America/Lower_Princes");
        addMapping("SA Western Standard Time", "TT", "America/Port_of_Spain");
        addMapping("SA Western Standard Time", "VC", "America/St_Vincent");
        addMapping("SA Western Standard Time", "VG", "America/Tortola");
        addMapping("SA Western Standard Time", "VI", "America/St_Thomas");
        addMapping("SA Western Standard Time", "ZZ", "Etc/GMT+4");
        // (UTC-04:00) Santiago  
        addMapping("Pacific SA Standard Time", "001", "America/Santiago");
        addMapping("Pacific SA Standard Time", "AQ", "Antarctica/Palmer");
        addMapping("Pacific SA Standard Time", "CL", "America/Santiago");
        // (UTC-03:30) Newfoundland  
        addMapping("Newfoundland Standard Time", "001", "America/St_Johns");
        addMapping("Newfoundland Standard Time", "CA", "America/St_Johns");
        // (UTC-03:00) Brasilia  
        addMapping("E. South America Standard Time", "001", "America/Sao_Paulo");
        addMapping("E. South America Standard Time", "BR", "America/Sao_Paulo America/Araguaina");
        // (UTC-03:00) Buenos Aires  
        addMapping("Argentina Standard Time", "001", "America/Buenos_Aires");
        addMapping("Argentina Standard Time", "AR", "America/Buenos_Aires America/Argentina/La_Rioja America/Argentina/Rio_Gallegos America/Argentina/Salta America/Argentina/San_Juan America/Argentina/San_Luis America/Argentina/Tucuman America/Argentina/Ushuaia America/Catamarca America/Cordoba America/Jujuy America/Mendoza");
        // (UTC-03:00) Cayenne, Fortaleza  
        addMapping("SA Eastern Standard Time", "001", "America/Cayenne");
        addMapping("SA Eastern Standard Time", "AQ", "Antarctica/Rothera");
        addMapping("SA Eastern Standard Time", "BR", "America/Fortaleza America/Belem America/Maceio America/Recife America/Santarem");
        addMapping("SA Eastern Standard Time", "FK", "Atlantic/Stanley");
        addMapping("SA Eastern Standard Time", "GF", "America/Cayenne");
        addMapping("SA Eastern Standard Time", "SR", "America/Paramaribo");
        addMapping("SA Eastern Standard Time", "ZZ", "Etc/GMT+3");
        // (UTC-03:00) Greenland  
        addMapping("Greenland Standard Time", "001", "America/Godthab");
        addMapping("Greenland Standard Time", "GL", "America/Godthab");
        // (UTC-03:00) Montevideo  
        addMapping("Montevideo Standard Time", "001", "America/Montevideo");
        addMapping("Montevideo Standard Time", "UY", "America/Montevideo");
        // (UTC-03:00) Salvador  
        addMapping("Bahia Standard Time", "001", "America/Bahia");
        addMapping("Bahia Standard Time", "BR", "America/Bahia");
        // (UTC-02:00) Coordinated Universal Time-02  
        addMapping("UTC-02", "001", "Etc/GMT+2");
        addMapping("UTC-02", "BR", "America/Noronha");
        addMapping("UTC-02", "GS", "Atlantic/South_Georgia");
        addMapping("UTC-02", "ZZ", "Etc/GMT+2");
        // (UTC-02:00) Mid-Atlantic  
        // Unmappable  
        // (UTC-01:00) Azores  
        addMapping("Azores Standard Time", "001", "Atlantic/Azores");
        addMapping("Azores Standard Time", "GL", "America/Scoresbysund");
        addMapping("Azores Standard Time", "PT", "Atlantic/Azores");
        // (UTC-01:00) Cape Verde Is.  
        addMapping("Cape Verde Standard Time", "001", "Atlantic/Cape_Verde");
        addMapping("Cape Verde Standard Time", "CV", "Atlantic/Cape_Verde");
        addMapping("Cape Verde Standard Time", "ZZ", "Etc/GMT+1");
        // (UTC) Casablanca  
        addMapping("Morocco Standard Time", "001", "Africa/Casablanca");
        addMapping("Morocco Standard Time", "MA", "Africa/Casablanca");
        // (UTC) Coordinated Universal Time  
        addMapping("UTC", "001", "Etc/GMT");
        addMapping("UTC", "GL", "America/Danmarkshavn");
        addMapping("UTC", "ZZ", "Etc/GMT");
        // (UTC) Dublin, Edinburgh, Lisbon, London  
        addMapping("GMT Standard Time", "001", "Europe/London");
        addMapping("GMT Standard Time", "ES", "Atlantic/Canary");
        addMapping("GMT Standard Time", "FO", "Atlantic/Faeroe");
        addMapping("GMT Standard Time", "GB", "Europe/London");
        addMapping("GMT Standard Time", "GG", "Europe/Guernsey");
        addMapping("GMT Standard Time", "IE", "Europe/Dublin");
        addMapping("GMT Standard Time", "IM", "Europe/Isle_of_Man");
        addMapping("GMT Standard Time", "JE", "Europe/Jersey");
        addMapping("GMT Standard Time", "PT", "Europe/Lisbon Atlantic/Madeira");
        // (UTC) Monrovia, Reykjavik  
        addMapping("Greenwich Standard Time", "001", "Atlantic/Reykjavik");
        addMapping("Greenwich Standard Time", "BF", "Africa/Ouagadougou");
        addMapping("Greenwich Standard Time", "CI", "Africa/Abidjan");
        addMapping("Greenwich Standard Time", "EH", "Africa/El_Aaiun");
        addMapping("Greenwich Standard Time", "GH", "Africa/Accra");
        addMapping("Greenwich Standard Time", "GM", "Africa/Banjul");
        addMapping("Greenwich Standard Time", "GN", "Africa/Conakry");
        addMapping("Greenwich Standard Time", "GW", "Africa/Bissau");
        addMapping("Greenwich Standard Time", "IS", "Atlantic/Reykjavik");
        addMapping("Greenwich Standard Time", "LR", "Africa/Monrovia");
        addMapping("Greenwich Standard Time", "ML", "Africa/Bamako");
        addMapping("Greenwich Standard Time", "MR", "Africa/Nouakchott");
        addMapping("Greenwich Standard Time", "SH", "Atlantic/St_Helena");
        addMapping("Greenwich Standard Time", "SL", "Africa/Freetown");
        addMapping("Greenwich Standard Time", "SN", "Africa/Dakar");
        addMapping("Greenwich Standard Time", "ST", "Africa/Sao_Tome");
        addMapping("Greenwich Standard Time", "TG", "Africa/Lome");
        // (UTC+01:00) Amsterdam, Berlin, Bern, Rome, Stockholm, Vienna
        addMapping("W. Europe Standard Time", "001", "Europe/Berlin");
        addMapping("W. Europe Standard Time", "AD", "Europe/Andorra");
        addMapping("W. Europe Standard Time", "AT", "Europe/Vienna");
        addMapping("W. Europe Standard Time", "CH", "Europe/Zurich");
        addMapping("W. Europe Standard Time", "DE", "Europe/Berlin Europe/Busingen");
        addMapping("W. Europe Standard Time", "GI", "Europe/Gibraltar");
        addMapping("W. Europe Standard Time", "IT", "Europe/Rome");
        addMapping("W. Europe Standard Time", "LI", "Europe/Vaduz");
        addMapping("W. Europe Standard Time", "LU", "Europe/Luxembourg");
        addMapping("W. Europe Standard Time", "MC", "Europe/Monaco");
        addMapping("W. Europe Standard Time", "MT", "Europe/Malta");
        addMapping("W. Europe Standard Time", "NL", "Europe/Amsterdam");
        addMapping("W. Europe Standard Time", "NO", "Europe/Oslo");
        addMapping("W. Europe Standard Time", "SE", "Europe/Stockholm");
        addMapping("W. Europe Standard Time", "SJ", "Arctic/Longyearbyen");
        addMapping("W. Europe Standard Time", "SM", "Europe/San_Marino");
        addMapping("W. Europe Standard Time", "VA", "Europe/Vatican");
        //(UTC+01:00) Belgrade, Bratislava, Budapest, Ljubljana, Prague
        addMapping("Central Europe Standard Time", "001", "Europe/Budapest");
        addMapping("Central Europe Standard Time", "AL", "Europe/Tirane");
        addMapping("Central Europe Standard Time", "CZ", "Europe/Prague");
        addMapping("Central Europe Standard Time", "HU", "Europe/Budapest");
        addMapping("Central Europe Standard Time", "ME", "Europe/Podgorica");
        addMapping("Central Europe Standard Time", "RS", "Europe/Belgrade");
        addMapping("Central Europe Standard Time", "SI", "Europe/Ljubljana");
        addMapping("Central Europe Standard Time", "SK", "Europe/Bratislava");
        // (UTC+01:00) Brussels, Copenhagen, Madrid, Paris  
        addMapping("Romance Standard Time", "001", "Europe/Paris");
        addMapping("Romance Standard Time", "BE", "Europe/Brussels");
        addMapping("Romance Standard Time", "DK", "Europe/Copenhagen");
        addMapping("Romance Standard Time", "ES", "Europe/Madrid Africa/Ceuta");
        addMapping("Romance Standard Time", "FR", "Europe/Paris");
        // (UTC+01:00) Sarajevo, Skopje, Warsaw, Zagreb  
        addMapping("Central European Standard Time", "001", "Europe/Warsaw");
        addMapping("Central European Standard Time", "BA", "Europe/Sarajevo");
        addMapping("Central European Standard Time", "HR", "Europe/Zagreb");
        addMapping("Central European Standard Time", "MK", "Europe/Skopje");
        addMapping("Central European Standard Time", "PL", "Europe/Warsaw");
        // (UTC+01:00) Tripoli  
        addMapping("Libya Standard Time", "001", "Africa/Tripoli");
        addMapping("Libya Standard Time", "LY", "Africa/Tripoli");
        // (UTC+01:00) West Central Africa  
        addMapping("W. Central Africa Standard Time", "001", "Africa/Lagos");
        addMapping("W. Central Africa Standard Time", "AO", "Africa/Luanda");
        addMapping("W. Central Africa Standard Time", "BJ", "Africa/Porto-Novo");
        addMapping("W. Central Africa Standard Time", "CD", "Africa/Kinshasa");
        addMapping("W. Central Africa Standard Time", "CF", "Africa/Bangui");
        addMapping("W. Central Africa Standard Time", "CG", "Africa/Brazzaville");
        addMapping("W. Central Africa Standard Time", "CM", "Africa/Douala");
        addMapping("W. Central Africa Standard Time", "DZ", "Africa/Algiers");
        addMapping("W. Central Africa Standard Time", "GA", "Africa/Libreville");
        addMapping("W. Central Africa Standard Time", "GQ", "Africa/Malabo");
        addMapping("W. Central Africa Standard Time", "NE", "Africa/Niamey");
        addMapping("W. Central Africa Standard Time", "NG", "Africa/Lagos");
        addMapping("W. Central Africa Standard Time", "TD", "Africa/Ndjamena");
        addMapping("W. Central Africa Standard Time", "TN", "Africa/Tunis");
        addMapping("W. Central Africa Standard Time", "ZZ", "Etc/GMT-1");
        // (UTC+01:00) Windhoek  
        addMapping("Namibia Standard Time", "001", "Africa/Windhoek");
        addMapping("Namibia Standard Time", "NA", "Africa/Windhoek");
        // (UTC+02:00) Athens, Bucharest  
        addMapping("GTB Standard Time", "001", "Europe/Bucharest");
        addMapping("GTB Standard Time", "GR", "Europe/Athens");
        addMapping("GTB Standard Time", "MD", "Europe/Chisinau");
        addMapping("GTB Standard Time", "RO", "Europe/Bucharest");
        // (UTC+02:00) Beirut  
        addMapping("Middle East Standard Time", "001", "Asia/Beirut");
        addMapping("Middle East Standard Time", "LB", "Asia/Beirut");
        // (UTC+02:00) Cairo  
        addMapping("Egypt Standard Time", "001", "Africa/Cairo");
        addMapping("Egypt Standard Time", "EG", "Africa/Cairo");
        // (UTC+02:00) Damascus  
        addMapping("Syria Standard Time", "001", "Asia/Damascus");
        addMapping("Syria Standard Time", "SY", "Asia/Damascus");
        // (UTC+02:00) E. Europe  
        addMapping("E. Europe Standard Time", "001", "Asia/Nicosia");
        addMapping("E. Europe Standard Time", "CY", "Asia/Nicosia");
        // (UTC+02:00) Harare, Pretoria  
        addMapping("South Africa Standard Time", "001", "Africa/Johannesburg");
        addMapping("South Africa Standard Time", "BI", "Africa/Bujumbura");
        addMapping("South Africa Standard Time", "BW", "Africa/Gaborone");
        addMapping("South Africa Standard Time", "CD", "Africa/Lubumbashi");
        addMapping("South Africa Standard Time", "LS", "Africa/Maseru");
        addMapping("South Africa Standard Time", "MW", "Africa/Blantyre");
        addMapping("South Africa Standard Time", "MZ", "Africa/Maputo");
        addMapping("South Africa Standard Time", "RW", "Africa/Kigali");
        addMapping("South Africa Standard Time", "SZ", "Africa/Mbabane");
        addMapping("South Africa Standard Time", "ZA", "Africa/Johannesburg");
        addMapping("South Africa Standard Time", "ZM", "Africa/Lusaka");
        addMapping("South Africa Standard Time", "ZW", "Africa/Harare");
        addMapping("South Africa Standard Time", "ZZ", "Etc/GMT-2");
        // (UTC+02:00) Helsinki, Kyiv, Riga, Sofia, Tallinn, Vilnius
        addMapping("FLE Standard Time", "001", "Europe/Kiev");
        addMapping("FLE Standard Time", "AX", "Europe/Mariehamn");
        addMapping("FLE Standard Time", "BG", "Europe/Sofia");
        addMapping("FLE Standard Time", "EE", "Europe/Tallinn");
        addMapping("FLE Standard Time", "FI", "Europe/Helsinki");
        addMapping("FLE Standard Time", "LT", "Europe/Vilnius");
        addMapping("FLE Standard Time", "LV", "Europe/Riga");
        addMapping("FLE Standard Time", "UA", "Europe/Kiev Europe/Simferopol Europe/Uzhgorod Europe/Zaporozhye");
        // (UTC+02:00) Istanbul  
        addMapping("Turkey Standard Time", "001", "Europe/Istanbul");
        addMapping("Turkey Standard Time", "TR", "Europe/Istanbul");
        // (UTC+02:00) Jerusalem  
        addMapping("Israel Standard Time", "001", "Asia/Jerusalem");
        addMapping("Israel Standard Time", "IL", "Asia/Jerusalem");
        // (UTC+03:00) Amman  
        addMapping("Jordan Standard Time", "001", "Asia/Amman");
        addMapping("Jordan Standard Time", "JO", "Asia/Amman");
        // (UTC+03:00) Baghdad  
        addMapping("Arabic Standard Time", "001", "Asia/Baghdad");
        addMapping("Arabic Standard Time", "IQ", "Asia/Baghdad");
        // (UTC+03:00) Kaliningrad, Minsk  
        addMapping("Kaliningrad Standard Time", "001", "Europe/Kaliningrad");
        addMapping("Kaliningrad Standard Time", "BY", "Europe/Minsk");
        addMapping("Kaliningrad Standard Time", "RU", "Europe/Kaliningrad");
        // (UTC+03:00) Kuwait, Riyadh  
        addMapping("Arab Standard Time", "001", "Asia/Riyadh");
        addMapping("Arab Standard Time", "BH", "Asia/Bahrain");
        addMapping("Arab Standard Time", "KW", "Asia/Kuwait");
        addMapping("Arab Standard Time", "QA", "Asia/Qatar");
        addMapping("Arab Standard Time", "SA", "Asia/Riyadh");
        addMapping("Arab Standard Time", "YE", "Asia/Aden");
        // (UTC+03:00) Nairobi  
        addMapping("E. Africa Standard Time", "001", "Africa/Nairobi");
        addMapping("E. Africa Standard Time", "AQ", "Antarctica/Syowa");
        addMapping("E. Africa Standard Time", "DJ", "Africa/Djibouti");
        addMapping("E. Africa Standard Time", "ER", "Africa/Asmera");
        addMapping("E. Africa Standard Time", "ET", "Africa/Addis_Ababa");
        addMapping("E. Africa Standard Time", "KE", "Africa/Nairobi");
        addMapping("E. Africa Standard Time", "KM", "Indian/Comoro");
        addMapping("E. Africa Standard Time", "MG", "Indian/Antananarivo");
        addMapping("E. Africa Standard Time", "SD", "Africa/Khartoum");
        addMapping("E. Africa Standard Time", "SO", "Africa/Mogadishu");
        addMapping("E. Africa Standard Time", "SS", "Africa/Juba");
        addMapping("E. Africa Standard Time", "TZ", "Africa/Dar_es_Salaam");
        addMapping("E. Africa Standard Time", "UG", "Africa/Kampala");
        addMapping("E. Africa Standard Time", "YT", "Indian/Mayotte");
        addMapping("E. Africa Standard Time", "ZZ", "Etc/GMT-3");
        // (UTC+03:30) Tehran  
        addMapping("Iran Standard Time", "001", "Asia/Tehran");
        addMapping("Iran Standard Time", "IR", "Asia/Tehran");
        // (UTC+04:00) Abu Dhabi, Muscat  
        addMapping("Arabian Standard Time", "001", "Asia/Dubai");
        addMapping("Arabian Standard Time", "AE", "Asia/Dubai");
        addMapping("Arabian Standard Time", "OM", "Asia/Muscat");
        addMapping("Arabian Standard Time", "ZZ", "Etc/GMT-4");
        // (UTC+04:00) Baku  
        addMapping("Azerbaijan Standard Time", "001", "Asia/Baku");
        addMapping("Azerbaijan Standard Time", "AZ", "Asia/Baku");
        // (UTC+04:00) Moscow, St. Petersburg, Volgograd  
        addMapping("Russian Standard Time", "001", "Europe/Moscow");
        addMapping("Russian Standard Time", "RU", "Europe/Moscow Europe/Samara Europe/Volgograd");
        // (UTC+04:00) Port Louis  
        addMapping("Mauritius Standard Time", "001", "Indian/Mauritius");
        addMapping("Mauritius Standard Time", "MU", "Indian/Mauritius");
        addMapping("Mauritius Standard Time", "RE", "Indian/Reunion");
        addMapping("Mauritius Standard Time", "SC", "Indian/Mahe");
        // (UTC+04:00) Tbilisi  
        addMapping("Georgian Standard Time", "001", "Asia/Tbilisi");
        addMapping("Georgian Standard Time", "GE", "Asia/Tbilisi");
        // (UTC+04:00) Yerevan  
        addMapping("Caucasus Standard Time", "001", "Asia/Yerevan");
        addMapping("Caucasus Standard Time", "AM", "Asia/Yerevan");
        // (UTC+04:30) Kabul  
        addMapping("Afghanistan Standard Time", "001", "Asia/Kabul");
        addMapping("Afghanistan Standard Time", "AF", "Asia/Kabul");
        // (UTC+05:00) Islamabad, Karachi  
        addMapping("Pakistan Standard Time", "001", "Asia/Karachi");
        addMapping("Pakistan Standard Time", "PK", "Asia/Karachi");
        // (UTC+05:00) Tashkent  
        addMapping("West Asia Standard Time", "001", "Asia/Tashkent");
        addMapping("West Asia Standard Time", "AQ", "Antarctica/Mawson");
        addMapping("West Asia Standard Time", "KZ", "Asia/Oral Asia/Aqtau Asia/Aqtobe");
        addMapping("West Asia Standard Time", "MV", "Indian/Maldives");
        addMapping("West Asia Standard Time", "TF", "Indian/Kerguelen");
        addMapping("West Asia Standard Time", "TJ", "Asia/Dushanbe");
        addMapping("West Asia Standard Time", "TM", "Asia/Ashgabat");
        addMapping("West Asia Standard Time", "UZ", "Asia/Tashkent Asia/Samarkand");
        addMapping("West Asia Standard Time", "ZZ", "Etc/GMT-5");
        // (UTC+05:30) Chennai, Kolkata, Mumbai, New Delhi  
        addMapping("India Standard Time", "001", "Asia/Calcutta");
        addMapping("India Standard Time", "IN", "Asia/Calcutta");
        // (UTC+05:30) Sri Jayawardenepura  
        addMapping("Sri Lanka Standard Time", "001", "Asia/Colombo");
        addMapping("Sri Lanka Standard Time", "LK", "Asia/Colombo");
        // (UTC+05:45) Kathmandu  
        addMapping("Nepal Standard Time", "001", "Asia/Katmandu");
        addMapping("Nepal Standard Time", "NP", "Asia/Katmandu");
        // (UTC+06:00) Astana  
        addMapping("Central Asia Standard Time", "001", "Asia/Almaty");
        addMapping("Central Asia Standard Time", "AQ", "Antarctica/Vostok");
        addMapping("Central Asia Standard Time", "IO", "Indian/Chagos");
        addMapping("Central Asia Standard Time", "KG", "Asia/Bishkek");
        addMapping("Central Asia Standard Time", "KZ", "Asia/Almaty Asia/Qyzylorda");
        addMapping("Central Asia Standard Time", "ZZ", "Etc/GMT-6");
        // (UTC+06:00) Dhaka  
        addMapping("Bangladesh Standard Time", "001", "Asia/Dhaka");
        addMapping("Bangladesh Standard Time", "BD", "Asia/Dhaka");
        addMapping("Bangladesh Standard Time", "BT", "Asia/Thimphu");
        // (UTC+06:00) Ekaterinburg  
        addMapping("Ekaterinburg Standard Time", "001", "Asia/Yekaterinburg");
        addMapping("Ekaterinburg Standard Time", "RU", "Asia/Yekaterinburg");
        // (UTC+06:30) Yangon (Rangoon)  
        addMapping("Myanmar Standard Time", "001", "Asia/Rangoon");
        addMapping("Myanmar Standard Time", "CC", "Indian/Cocos");
        addMapping("Myanmar Standard Time", "MM", "Asia/Rangoon");
        // (UTC+07:00) Bangkok, Hanoi, Jakarta  
        addMapping("SE Asia Standard Time", "001", "Asia/Bangkok");
        addMapping("SE Asia Standard Time", "AQ", "Antarctica/Davis");
        addMapping("SE Asia Standard Time", "CX", "Indian/Christmas");
        addMapping("SE Asia Standard Time", "ID", "Asia/Jakarta Asia/Pontianak");
        addMapping("SE Asia Standard Time", "KH", "Asia/Phnom_Penh");
        addMapping("SE Asia Standard Time", "LA", "Asia/Vientiane");
        addMapping("SE Asia Standard Time", "MN", "Asia/Hovd");
        addMapping("SE Asia Standard Time", "TH", "Asia/Bangkok");
        addMapping("SE Asia Standard Time", "VN", "Asia/Saigon");
        addMapping("SE Asia Standard Time", "ZZ", "Etc/GMT-7");
        // (UTC+07:00) Novosibirsk  
        addMapping("N. Central Asia Standard Time", "001", "Asia/Novosibirsk");
        addMapping("N. Central Asia Standard Time", "RU", "Asia/Novosibirsk Asia/Novokuznetsk Asia/Omsk");
        // (UTC+08:00) Beijing, Chongqing, Hong Kong, Urumqi  
        addMapping("China Standard Time", "001", "Asia/Shanghai");
        addMapping("China Standard Time", "CN", "Asia/Shanghai Asia/Chongqing Asia/Harbin Asia/Kashgar Asia/Urumqi");
        addMapping("China Standard Time", "HK", "Asia/Hong_Kong");
        addMapping("China Standard Time", "MO", "Asia/Macau");
        // (UTC+08:00) Krasnoyarsk  
        addMapping("North Asia Standard Time", "001", "Asia/Krasnoyarsk");
        addMapping("North Asia Standard Time", "RU", "Asia/Krasnoyarsk");
        // (UTC+08:00) Kuala Lumpur, Singapore  
        addMapping("Singapore Standard Time", "001", "Asia/Singapore");
        addMapping("Singapore Standard Time", "BN", "Asia/Brunei");
        addMapping("Singapore Standard Time", "ID", "Asia/Makassar");
        addMapping("Singapore Standard Time", "MY", "Asia/Kuala_Lumpur Asia/Kuching");
        addMapping("Singapore Standard Time", "PH", "Asia/Manila");
        addMapping("Singapore Standard Time", "SG", "Asia/Singapore");
        addMapping("Singapore Standard Time", "ZZ", "Etc/GMT-8");
        // (UTC+08:00) Perth  
        addMapping("W. Australia Standard Time", "001", "Australia/Perth");
        addMapping("W. Australia Standard Time", "AQ", "Antarctica/Casey");
        addMapping("W. Australia Standard Time", "AU", "Australia/Perth");
        // (UTC+08:00) Taipei  
        addMapping("Taipei Standard Time", "001", "Asia/Taipei");
        addMapping("Taipei Standard Time", "TW", "Asia/Taipei");
        // (UTC+08:00) Ulaanbaatar  
        addMapping("Ulaanbaatar Standard Time", "001", "Asia/Ulaanbaatar");
        addMapping("Ulaanbaatar Standard Time", "MN", "Asia/Ulaanbaatar Asia/Choibalsan");
        // (UTC+09:00) Irkutsk  
        addMapping("North Asia East Standard Time", "001", "Asia/Irkutsk");
        addMapping("North Asia East Standard Time", "RU", "Asia/Irkutsk");
        // (UTC+09:00) Osaka, Sapporo, Tokyo  
        addMapping("Tokyo Standard Time", "001", "Asia/Tokyo");
        addMapping("Tokyo Standard Time", "ID", "Asia/Jayapura");
        addMapping("Tokyo Standard Time", "JP", "Asia/Tokyo");
        addMapping("Tokyo Standard Time", "PW", "Pacific/Palau");
        addMapping("Tokyo Standard Time", "TL", "Asia/Dili");
        addMapping("Tokyo Standard Time", "ZZ", "Etc/GMT-9");
        // (UTC+09:00) Seoul  
        addMapping("Korea Standard Time", "001", "Asia/Seoul");
        addMapping("Korea Standard Time", "KP", "Asia/Pyongyang");
        addMapping("Korea Standard Time", "KR", "Asia/Seoul");
        // (UTC+09:30) Adelaide  
        addMapping("Cen. Australia Standard Time", "001", "Australia/Adelaide");
        addMapping("Cen. Australia Standard Time", "AU", "Australia/Adelaide Australia/Broken_Hill");
        // (UTC+09:30) Darwin  
        addMapping("AUS Central Standard Time", "001", "Australia/Darwin");
        addMapping("AUS Central Standard Time", "AU", "Australia/Darwin");
        // (UTC+10:00) Brisbane  
        addMapping("E. Australia Standard Time", "001", "Australia/Brisbane");
        addMapping("E. Australia Standard Time", "AU", "Australia/Brisbane Australia/Lindeman");
        // (UTC+10:00) Canberra, Melbourne, Sydney  
        addMapping("AUS Eastern Standard Time", "001", "Australia/Sydney");
        addMapping("AUS Eastern Standard Time", "AU", "Australia/Sydney Australia/Melbourne");
        // (UTC+10:00) Guam, Port Moresby  
        addMapping("West Pacific Standard Time", "001", "Pacific/Port_Moresby");
        addMapping("West Pacific Standard Time", "AQ", "Antarctica/DumontDUrville");
        addMapping("West Pacific Standard Time", "FM", "Pacific/Truk");
        addMapping("West Pacific Standard Time", "GU", "Pacific/Guam");
        addMapping("West Pacific Standard Time", "MP", "Pacific/Saipan");
        addMapping("West Pacific Standard Time", "PG", "Pacific/Port_Moresby");
        addMapping("West Pacific Standard Time", "ZZ", "Etc/GMT-10");
        // (UTC+10:00) Hobart  
        addMapping("Tasmania Standard Time", "001", "Australia/Hobart");
        addMapping("Tasmania Standard Time", "AU", "Australia/Hobart Australia/Currie");
        // (UTC+10:00) Yakutsk  
        addMapping("Yakutsk Standard Time", "001", "Asia/Yakutsk");
        addMapping("Yakutsk Standard Time", "RU", "Asia/Yakutsk Asia/Khandyga");
        // (UTC+11:00) Solomon Is., New Caledonia  
        addMapping("Central Pacific Standard Time", "001", "Pacific/Guadalcanal");
        addMapping("Central Pacific Standard Time", "AU", "Antarctica/Macquarie");
        addMapping("Central Pacific Standard Time", "FM", "Pacific/Ponape Pacific/Kosrae");
        addMapping("Central Pacific Standard Time", "NC", "Pacific/Noumea");
        addMapping("Central Pacific Standard Time", "SB", "Pacific/Guadalcanal");
        addMapping("Central Pacific Standard Time", "VU", "Pacific/Efate");
        addMapping("Central Pacific Standard Time", "ZZ", "Etc/GMT-11");
        // (UTC+11:00) Vladivostok  
        addMapping("Vladivostok Standard Time", "001", "Asia/Vladivostok");
        addMapping("Vladivostok Standard Time", "RU", "Asia/Vladivostok Asia/Sakhalin Asia/Ust-Nera");
        // (UTC+12:00) Auckland, Wellington  
        addMapping("New Zealand Standard Time", "001", "Pacific/Auckland");
        addMapping("New Zealand Standard Time", "AQ", "Antarctica/South_Pole Antarctica/McMurdo");
        addMapping("New Zealand Standard Time", "NZ", "Pacific/Auckland");
        // (UTC+12:00) Coordinated Universal Time+12  
        addMapping("UTC+12", "001", "Etc/GMT-12");
        addMapping("UTC+12", "KI", "Pacific/Tarawa");
        addMapping("UTC+12", "MH", "Pacific/Majuro Pacific/Kwajalein");
        addMapping("UTC+12", "NR", "Pacific/Nauru");
        addMapping("UTC+12", "TV", "Pacific/Funafuti");
        addMapping("UTC+12", "UM", "Pacific/Wake");
        addMapping("UTC+12", "WF", "Pacific/Wallis");
        addMapping("UTC+12", "ZZ", "Etc/GMT-12");
        // (UTC+12:00) Fiji  
        addMapping("Fiji Standard Time", "001", "Pacific/Fiji");
        addMapping("Fiji Standard Time", "FJ", "Pacific/Fiji");
        // (UTC+12:00) Magadan  
        addMapping("Magadan Standard Time", "001", "Asia/Magadan");
        addMapping("Magadan Standard Time", "RU", "Asia/Magadan Asia/Anadyr Asia/Kamchatka");
        // (UTC+13:00) Nuku'alofa  
        addMapping("Tonga Standard Time", "001", "Pacific/Tongatapu");
        addMapping("Tonga Standard Time", "KI", "Pacific/Enderbury");
        addMapping("Tonga Standard Time", "TK", "Pacific/Fakaofo");
        addMapping("Tonga Standard Time", "TO", "Pacific/Tongatapu");
        addMapping("Tonga Standard Time", "ZZ", "Etc/GMT-13");
        // (UTC+13:00) Samoa  
        addMapping("Samoa Standard Time", "001", "Pacific/Apia");
        addMapping("Samoa Standard Time", "WS", "Pacific/Apia");

    }

}
