package org.mtransit.parser.ca_niagara_region_transit_bus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;

import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mtransit.parser.StringUtils.EMPTY;

// http://www.niagararegion.ca/government/opendata/data-set.aspx#id=32
// https://maps.niagararegion.ca/googletransit/NiagaraRegionTransit.zip
public class NiagaraRegionTransitBusAgencyTools extends DefaultAgencyTools {

	public static void main(@Nullable String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-niagara-region-transit-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new NiagaraRegionTransitBusAgencyTools().start(args);
	}

	@Nullable
	private HashSet<Integer> serviceIdInts;

	@Override
	public void start(@NotNull String[] args) {
		MTLog.log("Generating Niagara Region Transit bus data...");
		long start = System.currentTimeMillis();
		this.serviceIdInts = extractUsefulServiceIdInts(args, this, true);
		super.start(args);
		MTLog.log("Generating Niagara Region Transit bus data... DONE in %s.", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIdInts != null && this.serviceIdInts.isEmpty();
	}

	@Override
	public boolean excludeCalendar(@NotNull GCalendar gCalendar) {
		if (this.serviceIdInts != null) {
			return excludeUselessCalendarInt(gCalendar, this.serviceIdInts);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(@NotNull GCalendarDate gCalendarDates) {
		if (this.serviceIdInts != null) {
			return excludeUselessCalendarDateInt(gCalendarDates, this.serviceIdInts);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(@NotNull GTrip gTrip) {
		if (this.serviceIdInts != null) {
			return excludeUselessTripInt(gTrip, this.serviceIdInts);
		}
		return super.excludeTrip(gTrip);
	}

	private static final String NIAGARA_REGION_TRANSIT = "Niagara Region Transit";

	private static final String EXCLUDE_STC_ROUTE_IDS_STARTS_WITH = "STC_";

	@Override
	public boolean excludeRoute(@NotNull GRoute gRoute) {
		//noinspection deprecation
		final String agencyId = gRoute.getAgencyIdOrDefault();
		if (!agencyId.contains(NIAGARA_REGION_TRANSIT)
				&& !agencyId.contains("AllNRT_")) {
			return true; // excluded
		}
		if (agencyId.contains("AllNRT_")) {
			if (!Utils.isDigitsOnly(gRoute.getRouteShortName())) {
				return true; // exclude
			}
			final int rsn = Integer.parseInt(gRoute.getRouteShortName());
			if (rsn > 100) {
				return true; // exclude
			}
		}
		//noinspection deprecation
		final String routeId = gRoute.getRouteId();
		if (routeId.startsWith(EXCLUDE_STC_ROUTE_IDS_STARTS_WITH)) {
			return true; // excluded
		}
		return super.excludeRoute(gRoute);
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	private static final String A = "A";

	private static final Pattern DIGITS = Pattern.compile("[\\d]+");

	@Override
	public long getRouteId(@NotNull GRoute gRoute) {
		if (gRoute.getRouteShortName().length() > 0
				&& Utils.isDigitsOnly(gRoute.getRouteShortName())) {
			return Long.parseLong(gRoute.getRouteShortName());
		}
		//noinspection deprecation
		Matcher matcher = DIGITS.matcher(gRoute.getRouteId());
		if (matcher.find()) {
			long id = Long.parseLong(matcher.group());
			if (gRoute.getRouteShortName().endsWith(A)) {
				return 10_000L + id;
			}
		}
		throw new MTLog.Fatal("Unexpected route ID for %s!", gRoute);
	}

	private static final Pattern STARTS_WITH_CT = Pattern.compile("(^(NRT - ))", Pattern.CASE_INSENSITIVE);
	private static final Pattern ENDS_WITH_CT = Pattern.compile("( (nf|sc|we)$)", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String getRouteLongName(@NotNull GRoute gRoute) {
		String routeLongName = gRoute.getRouteLongNameOrDefault();
		routeLongName = STARTS_WITH_CT.matcher(routeLongName).replaceAll(EMPTY);
		routeLongName = ENDS_WITH_CT.matcher(routeLongName).replaceAll(EMPTY);
		//noinspection deprecation
		routeLongName = CleanUtils.removePoints(routeLongName);
		return routeLongName;
	}

	private static final String AGENCY_COLOR_GREEN = "6CB33F"; // GREEN (from PDF)

	private static final String AGENCY_COLOR = AGENCY_COLOR_GREEN;

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@SuppressWarnings("DuplicateBranchesInSwitch")
	@Nullable
	@Override
	public String getRouteColor(@NotNull GRoute gRoute) {
		Matcher matcher = DIGITS.matcher(gRoute.getRouteShortName());
		if (matcher.find()) {
			int id = Integer.parseInt(matcher.group());
			switch (id) {
			// @formatter:off
			case 22: return "766A24";
			case 25: return "00AAA0"; // Welland Transit?
			case 40: return "1B5C28";
			case 45: return "1B5C28";
			case 50: return "F1471C";
			case 55: return "F1471C";
			case 60: return "1378C7";
			case 65: return "1378C7";
			case 70: return "62B92C";
			case 75: return "62B92C";
			// @formatter:on
			}
		}
		throw new MTLog.Fatal("Unexpected route color for %s!", gRoute);
	}

	private static final Pattern STARTS_WITH_NRT_A00_ = Pattern.compile( //
			"((^)((allnrt|nrt|)_([a-z]{1,3})?[\\d]{2,4}(_)?([A-Z]{3}(stop))?(stop)?)(NFT)?)", //
			Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String cleanStopOriginalId(@NotNull String gStopId) {
		gStopId = STARTS_WITH_NRT_A00_.matcher(gStopId).replaceAll(EMPTY);
		return gStopId;
	}

	@Override
	public void setTripHeadsign(@NotNull MRoute mRoute, @NotNull MTrip mTrip, @NotNull GTrip gTrip, @NotNull GSpec gtfs) {
		mTrip.setHeadsignString(
				cleanTripHeadsign(gTrip.getTripHeadsignOrDefault()),
				gTrip.getDirectionIdOrDefault()
		);
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	private static final Pattern STARTS_WITH_RSN = Pattern.compile("((^)[\\d]{2}([a-z] | ))", Pattern.CASE_INSENSITIVE);
	private static final String STARTS_WITH_RSN_REPLACEMENT = "$3";

	private static final Pattern IMT_ = Pattern.compile("((^|\\W)(imt -|imt)(\\W|$))", Pattern.CASE_INSENSITIVE); // Inter-Municipal Transit

	private static final String ST_CATHARINES = "St Catharines";
	private static final Pattern ST_CATHARINES_ = Pattern.compile("((^|\\W)(" //
			+ "st\\. catharines" + "|" //
			+ "st\\. catharine" + "|" //
			+ "st\\. catharin" + "|" //
			+ "st\\. cathari" + "|" //
			+ "st\\. cathar" + "|" //
			+ "st\\. catha" + "|" //
			+ "st\\. cath" + "|" //
			+ "st\\. cat" + "|" //
			+ "st\\. ca" + "|" //
			+ "st\\. c" + "|" //
			+ "st\\. " //
			+ ")(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String ST_CATHARINES_REPLACEMENT = "$2" + ST_CATHARINES + "$4";

	private static final String NIAGARA_FALLS = "Niagara Falls";
	private static final Pattern NIAGARA_FALLS_ = Pattern.compile("((^|\\W)(" //
			+ "niagara falls" + "|" //
			+ "niagara fall" + "|" //
			+ "niag" + "|" //
			+ "nia\\. falls" + "|" //
			+ "falls" //
			+ ")(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String NIAGARA_FALLS_REPLACEMENT = "$2" + NIAGARA_FALLS + "$4";

	private static final String OUTLET_MALL = "Outlet Mall";
	private static final Pattern OUTLET_MALL_ = Pattern.compile("((^|\\W)(" //
			+ "outlet mall" + "|" //
			+ "outlet m" //
			+ ")(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String OUTLET_MALL_REPLACEMENT = "$2" + OUTLET_MALL + "$4";

	private static final String WELLAND = "Welland";
	private static final Pattern WELLAND_ = Pattern.compile("((^|\\W)(" //
			+ "welland" + "|" //
			+ "wellan" //
			+ ")(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String WELLAND_REPLACEMENT = "$2" + WELLAND + "$4";

	private static final Pattern NOTL_ = Pattern.compile("((^|\\W)(" //
			+ "notl"
			+ ")(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String NOTL_REPLACEMENT_ = "$2" + "NOTL" + "$4";

	private static final Pattern AND_NO_SPACE = Pattern.compile("(([\\S])\\s?([&@])\\s?([\\S]))", Pattern.CASE_INSENSITIVE);
	private static final String AND_NO_SPACE_REPLACEMENT = "$2 $3 $4";

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = AND_NO_SPACE.matcher(tripHeadsign).replaceAll(AND_NO_SPACE_REPLACEMENT);
		tripHeadsign = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, tripHeadsign, getIgnoredWords());
		tripHeadsign = STARTS_WITH_RSN.matcher(tripHeadsign).replaceAll(STARTS_WITH_RSN_REPLACEMENT);
		tripHeadsign = IMT_.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = ST_CATHARINES_.matcher(tripHeadsign).replaceAll(ST_CATHARINES_REPLACEMENT);
		tripHeadsign = NIAGARA_FALLS_.matcher(tripHeadsign).replaceAll(NIAGARA_FALLS_REPLACEMENT);
		tripHeadsign = NOTL_.matcher(tripHeadsign).replaceAll(NOTL_REPLACEMENT_);
		tripHeadsign = OUTLET_MALL_.matcher(tripHeadsign).replaceAll(OUTLET_MALL_REPLACEMENT);
		tripHeadsign = WELLAND_.matcher(tripHeadsign).replaceAll(WELLAND_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanBounds(tripHeadsign);
		tripHeadsign = CleanUtils.keepToAndRemoveVia(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	@Override
	public boolean mergeHeadsign(@NotNull MTrip mTrip, @NotNull MTrip mTripToMerge) {
		throw new MTLog.Fatal("Unexpected trips to merge %s & %s!", mTrip, mTripToMerge);
	}

	private String[] getIgnoredWords() {
		return new String[]{
				"NE", "NW", "SE", "SW",
				"GO", "NF", "NOTL",
		};
	}

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = AND_NO_SPACE.matcher(gStopName).replaceAll(AND_NO_SPACE_REPLACEMENT);
		gStopName = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, gStopName, getIgnoredWords());
		gStopName = CleanUtils.CLEAN_AND.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = CleanUtils.cleanBounds(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	@NotNull
	@Override
	public String getStopCode(@NotNull GStop gStop) {
		if (ZERO_0.equals(gStop.getStopCode())) {
			return EMPTY; // no stop code
		}
		return super.getStopCode(gStop);
	}

	private static final String ZERO_0 = "0";

	@Override
	public int getStopId(@NotNull GStop gStop) {
		//noinspection deprecation
		final String stopId1 = gStop.getStopId();
		//noinspection ConstantConditions
		if (true) {
			String stopId = stopId1;
			stopId = STARTS_WITH_NRT_A00_.matcher(stopId).replaceAll(EMPTY);
			if (stopId.isEmpty()) {
				throw new MTLog.Fatal("Unexpected stop ID (%d) %s!", stopId, gStop.toStringPlus());
			}
			if (Utils.isDigitsOnly(stopId)) {
				return Integer.parseInt(stopId);
			}
			throw new MTLog.Fatal("Unexpected stop ID %s!", gStop.toStringPlus());
		}
		String stopCode = gStop.getStopCode();
		if (stopCode == null || stopCode.length() == 0 || ZERO_0.equals(stopCode)) {
			stopCode = stopId1;
		}
		stopCode = STARTS_WITH_NRT_A00_.matcher(stopCode).replaceAll(EMPTY);
		if (stopCode.isEmpty()) {
			throw new MTLog.Fatal("Unexpected empty stop ID %s!", gStop.toStringPlus());
		}
		if (Utils.isDigitsOnly(stopCode)) {
			return Integer.parseInt(stopCode); // using stop code as stop ID
		}
		switch (stopCode) {
		case "DTT":
			return 100_000;
		case "NFT":
			return 100_001;
		case "PEN":
			return 100_002;
		case "SWM":
			return 100_003;
		case "WEL":
			return 100004;
		case "BRU":
			return 100_006;
		case "FVM":
			return 100_008;
		case "GDC":
			return 100_020;
		case "WLC":
			return 100_021;
		case "CTO":
			return 100_035;
		case "OUT":
			return 100_044;
		case "MCC":
			return 100_045;
		}
		switch (stopCode) {
		case "NiagSqua":
			return 200_000;
		case "Concentrix":
			return 200_001;
		case "FortErie":
			return 200_002;
		}
		if (stopCode.equals("PCH")) {
			return 9_000_016;
		}
		throw new MTLog.Fatal("Unexpected stop ID %s!", gStop.toStringPlus());
	}
}
